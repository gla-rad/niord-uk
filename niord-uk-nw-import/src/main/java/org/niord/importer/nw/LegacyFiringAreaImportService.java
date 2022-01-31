/*
 * Copyright 2016 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.niord.importer.nw;

import org.apache.commons.lang.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.niord.core.area.Area;
import org.niord.core.area.AreaSearchParams;
import org.niord.core.area.AreaService;
import org.niord.core.area.AreaType;
import org.niord.core.category.Category;
import org.niord.core.chart.Chart;
import org.niord.core.conf.TextResource;
import org.niord.core.domain.Domain;
import org.niord.core.domain.DomainService;
import org.niord.core.geojson.Feature;
import org.niord.core.geojson.FeatureCollection;
import org.niord.core.geojson.JtsConverter;
import org.niord.core.message.Message;
import org.niord.core.message.MessagePart;
import org.niord.core.message.MessageSeries;
import org.niord.core.message.MessageSeriesService;
import org.niord.core.schedule.FiringPeriod;
import org.niord.core.schedule.FiringScheduleService;
import org.niord.core.settings.SettingsService;
import org.niord.core.settings.annotation.Setting;
import org.niord.core.util.TimeUtils;
import org.niord.model.geojson.PointVo;
import org.niord.model.geojson.PolygonVo;
import org.niord.model.message.MainType;
import org.niord.model.message.MessagePartType;
import org.niord.model.message.Status;
import org.niord.model.message.Type;
import org.slf4j.Logger;

import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Imports firing areas from a local db dump of the Danish MSI database
 */
@RequestScoped
public class LegacyFiringAreaImportService {

    public static Pattern FIRING_AREA_NAME_FORMAT_1 = Pattern.compile(
            "^(?<id>\\d+) .+$",
            Pattern.CASE_INSENSITIVE
    );
    public static Pattern FIRING_AREA_NAME_FORMAT_2 = Pattern.compile(
            "^(?<id>(EK|ES) (D|R) \\d+) .+$",
            Pattern.CASE_INSENSITIVE
    );


    /**
     * Registers whether or not to auto-import the firing exercise schedule.
     */
    private static final org.niord.core.settings.Setting LEGACY_AUTO_IMPORT_FE_SCHEDULE
            = new org.niord.core.settings.Setting("legacyAutoImportFeSchedule")
            .type(org.niord.core.settings.Setting.Type.Boolean)
            .value(false)
            .description("Auto-import flag for the import of legacy firing exercise schedule")
            .editable(true)
            .cached(false);



    @Inject
    Logger log;

    @Inject
    @TextResource("/sql/fa_all_firing_areas.sql")
    String allFiringAreasSql;

    @Inject
    @TextResource("/sql/fa_location_data.sql")
    String geometrySql;

    @Inject
    @TextResource("/sql/fa_information_data.sql")
    String infoSql;

    @Inject
    @TextResource("/sql/fa_periods.sql")
    String firingAreaPeriodsSql;

    String validateAreaIdSql = "select id from firing_area where id = ?";

    @Inject
    @Setting(value = "faMrnPrefix", defaultValue = "urn:mrn:iho:fa:dk:", web = true,
            description = "The MRN prefix to use for NM messages")
    String faMrnPrefix;

    @Inject
    LegacyNwDatabase db;

    @Inject
    AreaService areaService;

    @Inject
    FiringScheduleService firingScheduleService;

    @Inject
    MessageSeriesService messageSeriesService;

    @Inject
    SettingsService settingsService;

    @Inject
    DomainService domainService;


    /***************************************/
    /** Firing Area Import                **/
    /***************************************/


    /**
     * Imports the firing areas
     * @param importDb whether to import the database first
     */
    public Map<Integer, Area> importFiringAreas(boolean importDb) throws Exception {

        if (importDb) {
            db.downloadAndImportLegacyNwDump();
        }

        try (Connection con = db.openConnection();
             PreparedStatement faStmt = con.prepareStatement(allFiringAreasSql);
             PreparedStatement geoStmt = con.prepareStatement(geometrySql)) {


            // First load all areas
            Map<Integer, Area> areas = new LinkedHashMap<>();
            ResultSet rs = faStmt.executeQuery();
            int siblingSortOrder = 0;
            while (rs.next()) {
                Integer id          = getInt(rs, "id");
                Integer active      = getInt(rs, "active");
                String  area1En     = getString(rs, "area1_en");
                String  area1Da     = getString(rs, "area1_da");
                String  area2En     = getString(rs, "area2_en");
                String  area2Da     = getString(rs, "area2_da");
                String  area3En     = getString(rs, "area3_en");
                String  area3Da     = getString(rs, "area3_da");

                Area area = createAreaTemplate(area1En, area1Da, null);
                area = createAreaTemplate(area2En, area2Da, area);
                area = createAreaTemplate(area3En, area3Da, area);
                area.setActive(active == 1);
                area.setLegacyId(String.valueOf(id));
                area.setMrn(generateAreaMrn(area));
                area.setSiblingSortOrder(siblingSortOrder++);
                areas.put(id, area);
            }
            rs.close();


            // Next, load area locations
            for (Map.Entry<Integer, Area> a : areas.entrySet()) {
                log.debug("Reading geometry for firing area " + a.getKey());
                geoStmt.setInt(1, a.getKey());


                ResultSet grs = geoStmt.executeQuery();
                List<double[]> coords = new ArrayList<>();
                while (grs.next()) {
                    Integer latDeg      = getInt(grs, "lat_deg");
                    Double  latMin      = getDouble(grs, "lat_min");
                    String  latDir      = getString(grs, "lat_dir");
                    Integer lonDeg      = getInt(grs, "lon_deg");
                    Double  lonMin      = getDouble(grs, "lon_min");
                    String  lonDir      = getString(grs, "lon_dir");

                    if (latDeg != null && latMin != null && lonDeg != null && lonMin != null) {
                        double lon = lonDeg.doubleValue() + lonMin / 60.0;
                        if ("W".equalsIgnoreCase(lonDir)) {
                            lon = -lon;
                        }
                        double lat = latDeg.doubleValue() + latMin / 60.0;
                        if ("S".equalsIgnoreCase(latDir)) {
                            lat = -lat;
                        }
                        coords.add(new double[]{ lon, lat });
                    }
                }
                grs.close();

                // Only handle points and polygons
                Geometry geometry = null;
                if (coords.size() == 1) {
                    PointVo pt = new PointVo();
                    pt.setCoordinates(coords.get(0));
                    geometry = JtsConverter.toJts(pt);
                } else if (coords.size() > 2) {
                    // GeoJSON linear rings has the same start and end coordinate
                    coords.add(coords.get(0));
                    double[][] ring = new double[coords.size()][];
                    for (int x = 0; x < coords.size(); x++) {
                        ring[x] = coords.get(x);
                    }
                    PolygonVo p = new PolygonVo();
                    p.setCoordinates(new double[][][] { ring });
                    geometry = JtsConverter.toJts(p);
                }

                if (geometry != null) {
                    a.getValue().setGeometry(geometry);
                }
            }


            log.info("Fetched " + areas.size() + " legacy Firing Areas");
            return areas;
        }
    }


    /**
     * Merges the given area template into the area tree
     * @param areaTemplate the area to merge into the three
     */
    @TransactionAttribute(REQUIRES_NEW)
    public void mergeArea(Area areaTemplate) throws Exception {

        try {
            Area area = areaService.importArea(areaTemplate, true, false);
            if (area != null) {
                // If an existing area was found, check if we need to updated various fields
                boolean updated = false;
                if (area.isActive() != areaTemplate.isActive()) {
                    area.setActive(areaTemplate.isActive());
                    area.updateActiveFlag();
                    updated = true;
                    log.info("Updated active flag of area " + area.getId());
                }
                if (area.getGeometry() == null && areaTemplate.getGeometry() != null) {
                    area.setGeometry(areaTemplate.getGeometry());
                    updated = true;
                    log.info("Updated geometry of area " + area.getId());
                }
                if (area.getType() != AreaType.FIRING_AREA) {
                    area.setType(AreaType.FIRING_AREA);
                    updated = true;
                    log.info("Updated type of area " + area.getId());
                }
                if (area.getLegacyId() == null && areaTemplate.getLegacyId() != null) {
                    area.setLegacyId(areaTemplate.getLegacyId());
                    updated = true;
                    log.info("Updated legacy-ID of area " + area.getId());
                }
                if (area.getMrn() == null && areaTemplate.getMrn() != null) {
                    area.setMrn(areaTemplate.getMrn());
                    updated = true;
                    log.info("Updated MRN of area " + area.getId());
                }
                if (updated) {
                    areaService.saveEntity(area);
                }
            }
        } catch (Exception e) {
            log.error("Error updating imported legacy firing area " + areaTemplate.getDescs());
            throw e;
        }
    }


    /**
     * Creates an Area template based on the given Danish and English name
     * and optionally a parent Area
     * @param nameEn English name
     * @param nameDa Danish name
     * @param parent parent area
     * @return the Area template, or null if the names are empty
     */
    public static Area createAreaTemplate(String nameEn, String nameDa, Area parent) {
        Area area = null;
        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            area = new Area();
            if (StringUtils.isNotBlank(nameEn)) {
                area.createDesc("en").setName(nameEn);
            }
            if (StringUtils.isNotBlank(nameDa)) {
                area.createDesc("da").setName(nameDa);
            }
            area.setParent(parent);
        }
        return area;
    }


    /***************************************/
    /** Firing Exercise Schedule          **/
    /***************************************/


    /** Returns the auto-import flag */
    public Boolean getAutoImportFeSchedule() {
        return settingsService.getBoolean(LEGACY_AUTO_IMPORT_FE_SCHEDULE);
    }


    /** Updates the auto-import flag */
    public void updateAutoImportFeSchedule(Boolean autoImport) {
        settingsService.set(LEGACY_AUTO_IMPORT_FE_SCHEDULE.getKey(), autoImport);
    }


    /**
     * Imports the firing area schedule
     * @param importDb whether to import the database first
     * @param result a textual result
     */
    public List<FiringPeriod> importFiringAreaSchedule(boolean importDb, StringBuilder result) throws Exception {

        if (importDb) {
            db.downloadAndImportLegacyNwDump();
        }

        Map<Integer, Area> areaLookup = new HashMap<>();

        // Fetch all firing periods
        List<FiringPeriod> firingPeriods = new ArrayList<>();
        try (Connection con = db.openConnection();
             PreparedStatement stmt = con.prepareStatement(firingAreaPeriodsSql)) {

            // First load all firing area periods
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer id      = getInt(rs, "id");
                Date created    = getDate(rs, "creation_time");
                Date fromDate   = getDate(rs, "t_from");
                Date toDate     = getDate(rs, "t_to");
                Integer areaId  = getInt(rs, "f_area_id");

                // Look up and cache area by ID
                Area area = areaLookup.get(areaId);
                if (area == null) {
                    area = areaService.findByLegacyId(String.valueOf(areaId));
                    if (area == null) {
                        continue;
                    }
                    areaLookup.put(areaId, area);
                }

                // Create a new firing period template
                FiringPeriod fp = new FiringPeriod();
                fp.setLegacyId(String.valueOf(id));
                fp.setCreated(created);
                fp.setFromDate(TimeUtils.resetSeconds(fromDate));
                fp.setToDate(TimeUtils.resetSeconds(toDate));
                fp.setArea(area);

                firingPeriods.add(fp);
            }
            rs.close();
            log.debug("Loaded " + firingPeriods.size() + " firing periods from legacy system");

            // Merge the legacy firing periods with the currently imported firing periods
            mergeLegacyFiringPeriods(firingPeriods, result);

            return firingPeriods;
        }
    }


    /**
     * Merges the given legacy firing periods with the currently imported firing periods
     * @param firingPeriods the legacy firing periods
     * @param result a textual result
     */
    private void mergeLegacyFiringPeriods(List<FiringPeriod> firingPeriods, StringBuilder result) {

        // Get the currently persisted firing periods
        List<FiringPeriod> currentFiringPeriods = firingScheduleService.getAllLegacyFiringPeriods();

        // Create look-up maps for faster comparison
        Map<String, FiringPeriod> lookup = currentFiringPeriods.stream()
                .collect(Collectors.toMap(FiringPeriod::getLegacyId, Function.identity()));

        int added = 0, updated = 0, deleted = 0, ignored = 0;
        for (FiringPeriod fp : firingPeriods) {
            String legacyId = fp.getLegacyId();
            if (!lookup.containsKey(legacyId)) {
                firingScheduleService.addFiringPeriod(fp);
                added++;
            } else {
                FiringPeriod original = lookup.get(legacyId);
                if (original.hasChanged(fp)) {
                    original.updateFiringPeriod(fp);
                    firingScheduleService.saveEntity(original);
                    updated++;
                } else {
                    ignored++;
                }
                // NB: We want the look up to eventually contain all deleted firing periods
                lookup.remove(legacyId);
            }
        }

        // The lookup map should now contains all deleted firing periods
        for (FiringPeriod fp : lookup.values()) {
            firingScheduleService.deleteFiringPeriod(fp.getId());
            deleted++;
        }

        String txt = String.format("Added %d, updated %d, deleted %d, ignored %d firing periods",
                added, updated, deleted, ignored);
        result.append(txt).append("\n");
        if (added + updated + deleted > 0) {
            log.info(txt);
        }
    }


    /***************************************/
    /** Firing Area Message Templates     **/
    /***************************************/


    /** Creates message templates for all firing areas **/
    public List<Message> generateFiringAreaMessageTemplates(String seriesId) {

        MessageSeries messageSeries = messageSeriesService.findBySeriesId(seriesId);
        if (messageSeries == null) {
            return Collections.emptyList();
        }

        // Get hold of the list of firing areas for the current domain
        Domain domain = domainService.currentDomain();
        AreaSearchParams params = new AreaSearchParams()
                .inactive(false) // Do not include inactive areas
                .domain(domain != null ? domain.getDomainId() : null)
                .type(AreaType.FIRING_AREA);
        List<Area> firingAreas = areaService.searchAreas(params);

        List<Message> messages = new ArrayList<>();
        for (Area area : firingAreas) {
            Message message = createMessageTemplateForArea(area, messageSeries);
            if (message != null) {
                messages.add(message);
            }
        }

        return messages;
    }


    /**
     * Creates a message template for the given firing area
     * @param area the firing area
     * @param messageSeries the message series to use for the message
     * @return the message template
     */
    private Message createMessageTemplateForArea(Area area, MessageSeries messageSeries) {
        Integer id = getLegacyIdForArea(area);
        if (id == null) {
            return null;
        }

        int year = TimeUtils.getCalendarField(new Date(), Calendar.YEAR);

        Message message = new Message();
        message.setMessageSeries(messageSeries);
        message.setMainType(MainType.NM);
        message.setType(Type.MISCELLANEOUS_NOTICE);
        message.setStatus(Status.DRAFT);
        message.setShortId(extractAreaShortId(area, "FA/", " " + year));
        message.setAutoTitle(true);
        message.getAreas().add(area);

        // Read description fields
        Map<Integer, String> daInfo = new HashMap<>();
        Map<Integer, String> enInfo = new HashMap<>();
        getLegacyInformationForArea(id, daInfo, enInfo);

        // Fill out the description fields
        // 1: Details
        MessagePart detailPart = null;
        if (StringUtils.isNotBlank(daInfo.get(1)) || StringUtils.isNotBlank(enInfo.get(1))) {
            detailPart = new MessagePart(MessagePartType.DETAILS);
            message.addPart(detailPart);
            composeMessagePartDesc(detailPart, "da", daInfo.get(1));
            composeMessagePartDesc(detailPart, "en", enInfo.get(1));
        }
        // 2: Note
        if (StringUtils.isNotBlank(daInfo.get(2)) || StringUtils.isNotBlank(enInfo.get(2))) {
            MessagePart part = new MessagePart(MessagePartType.NOTE);
            message.addPart(part);
            composeMessagePartDesc(part, "da", daInfo.get(2));
            composeMessagePartDesc(part, "en", enInfo.get(2));
        }
        // 5: Prohibition
        if (StringUtils.isNotBlank(daInfo.get(5)) || StringUtils.isNotBlank(enInfo.get(5))) {
            MessagePart part = new MessagePart(MessagePartType.PROHIBITION);
            message.addPart(part);
            composeMessagePartDesc(part, "da", daInfo.get(5));
            composeMessagePartDesc(part, "en", enInfo.get(5));
        }
        // 6: Signals
        if (StringUtils.isNotBlank(daInfo.get(6)) || StringUtils.isNotBlank(enInfo.get(6))) {
            MessagePart part = new MessagePart(MessagePartType.SIGNALS);
            message.addPart(part);
            composeMessagePartDesc(part, "da", daInfo.get(6));
            composeMessagePartDesc(part, "en", enInfo.get(6));
        }

        if (area.getGeometry() != null) {
            FeatureCollection featureCollection = new FeatureCollection();
            Feature feature = new Feature();
            featureCollection.addFeature(feature);
            feature.setGeometry(area.getGeometry());
            if (area.getDesc("da") != null && StringUtils.isNotBlank(area.getDesc("da").getName())) {
                feature.getProperties().put("name:da", area.getDesc("da").getName());
            }
            if (area.getDesc("en") != null && StringUtils.isNotBlank(area.getDesc("en").getName())) {
                feature.getProperties().put("name:en", area.getDesc("en").getName());
            }
            if (detailPart == null) {
                detailPart = new MessagePart(MessagePartType.DETAILS);
                message.addPart(detailPart);
            }
            detailPart.setGeometry(featureCollection);
        }

        // Update the title line
        message.updateMessageTitle();

        // Charts
        String chartInfo = daInfo.get(3);
        if (StringUtils.isNotBlank(chartInfo)) {
            chartInfo = chartInfo.replace(".", ""); // Remove trailing blanks
            Arrays.stream(chartInfo.split(","))
                    .map(Chart::parse)
                    .filter(Objects::nonNull)
                    .forEach(c -> message.getCharts().add(c));
        }

        // Categories
        Category firingExercise = new Category();
        firingExercise.createDesc("da").setName("Skydeøvelser");
        firingExercise.createDesc("en").setName("Firing Exercises");
        message.getCategories().add(firingExercise);

        return message;
    }


    /**
     * Extracts the short id from an area name.
     * Examples:
     *   "ES D 139 Bornholm E." -> "ES D 139"
     *   "13 Seden" -> "13"
     **/
    private String extractAreaLegacyId(Area area) {
        if (area.getDesc("da") != null && StringUtils.isNotBlank(area.getDesc("da").getName())) {
            String name = area.getDesc("da").getName();

            Matcher m = FIRING_AREA_NAME_FORMAT_1.matcher(name);
            if (m.find()) {
                return m.group("id");
            }
            m = FIRING_AREA_NAME_FORMAT_2.matcher(name);
            if (m.find()) {
                return m.group("id");
            }
        }
        return null;
    }


    /**
     * Extracts the short id from an area name.
     * Examples:
     *   "ES D 139 Bornholm E." -> "FA/ES-D-139 2016"
     *   "13 Seden" -> "FA/13 2016"
     **/
    private String extractAreaShortId(Area area, String prefix, String postfix) {
        String shortId = extractAreaLegacyId(area);
        if (shortId != null) {
            return StringUtils.defaultString(prefix)
                    + shortId.replace(" ", "-")
                    + StringUtils.defaultString(postfix);
        }
        return null;
    }


    /**
     * Generates the MRN from an area name.
     * Examples:
     *   "ES D 139 Bornholm E." -> "urn:mrn:iho:fa:dk:ed-d-139"
     *   "13 Seden" -> "urn:mrn:iho:fa:dk:13"
     **/
    private String generateAreaMrn(Area area) {
        String shortId = extractAreaLegacyId(area);
        if (shortId != null) {
            return faMrnPrefix + shortId.toLowerCase().replace(" ", "-");
        }
        return null;
    }


    /** Composes the message part descriptor from the legacy firing area information **/
    private void composeMessagePartDesc(MessagePart part, String lang, String details) {
        if (StringUtils.isNotBlank(details)) {
            part.createDesc(lang).setDetails("<p>" + details.replace("\n", "<br>") + "</p>");
        }
    }


    /** Looks up the legacy id for the given area **/
    private Integer getLegacyIdForArea(Area area) {
        String legacyId = area.getLegacyId();
        if (!StringUtils.isNumeric(legacyId)) {
            return null;
        }
        Integer id = Integer.valueOf(legacyId);

        try (Connection con = db.openConnection();
             PreparedStatement stmt = con.prepareStatement(validateAreaIdSql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return getInt(rs, "id");
            }
        } catch (SQLException ignored) {
        }
        return null;
    }


    /** Loads the information associated with the legacy firing area **/
    private void getLegacyInformationForArea(Integer id, Map<Integer, String> daInfo, Map<Integer, String> enInfo) {
        try (Connection con = db.openConnection();
             PreparedStatement stmt = con.prepareStatement(infoSql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer infoType    = getInt(rs, "info_type");
                String  descDa      = getString(rs, "description_da");
                String  descEn      = getString(rs, "description_en");
                if (infoType != null) {
                    if (StringUtils.isNotBlank(descDa)) {
                        daInfo.put(infoType, descDa);
                    }
                    if (StringUtils.isNotBlank(descEn)) {
                        enInfo.put(infoType, descEn);
                    }
                }
            }
            rs.close();
        } catch (SQLException ignored) {
        }
    }


    /*************************/
    /** ResultSet accessors **/
    /*************************/


    String getString(ResultSet rs, String key) throws SQLException {
        String val = rs.getString(key);
        return rs.wasNull() ? null : val;
    }

    Integer getInt(ResultSet rs, String key) throws SQLException {
        Integer val = rs.getInt(key);
        return rs.wasNull() ? null : val;
    }

    Double getDouble(ResultSet rs, String key) throws SQLException {
        Double val = rs.getDouble(key);
        return rs.wasNull() ? null : val;
    }

    Date getDate(ResultSet rs, String key) throws SQLException {
        Timestamp val = rs.getTimestamp(key);
        return rs.wasNull() ? null : val;
    }
}

