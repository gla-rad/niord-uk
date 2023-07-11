/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.niord.uk.promulgation;

import org.apache.commons.lang.StringUtils;
import org.niord.core.dictionary.DictionaryService;
import org.niord.core.message.vo.SystemMessageVo;
import org.niord.core.promulgation.BasePromulgationService;
import org.niord.core.promulgation.PromulgationException;
import org.niord.core.promulgation.PromulgationType;
import org.niord.core.promulgation.PromulgationType.Requirement;
import org.niord.uk.promulgation.vo.AudioMessagePromulgationVo;
import org.niord.core.promulgation.vo.BaseMessagePromulgationVo;
import org.niord.core.util.PositionAssembler;
import org.niord.core.util.PositionUtils;
import org.niord.core.util.TextUtils;
import org.niord.model.DataFilter;
import org.niord.model.message.MessagePartType;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ResourceBundle;

/**
 * Manages audio promulgations.
 *
 * Audio promulgations are verbose textual versions of the messages suitable for being read up on the radio
 * and sent to the radio station via e-mail.
 */
@ApplicationScoped
@Lock(LockType.READ)
@SuppressWarnings("unused")
public class AudioPromulgationService extends BasePromulgationService {

    @Inject
    DictionaryService dictionaryService;

    /***************************************/
    /** Promulgation Service Handling     **/
    /***************************************/

    /** {@inheritDoc} */
    @Override
    public String getServiceId() {
        return AudioMessagePromulgation.SERVICE_ID;
    }


    /** {@inheritDoc} */
    @Override
    public String getServiceName() {
        return "Audio mailing list";
    }


    /***************************************/
    /** Message Life-cycle Management     **/
    /***************************************/


    /** {@inheritDoc} */
    @Override
    public void onLoadSystemMessage(SystemMessageVo message, PromulgationType type) throws PromulgationException {
        AudioMessagePromulgationVo audio = message.promulgation(AudioMessagePromulgationVo.class, type.getTypeId());
        if (audio == null) {
            audio = new AudioMessagePromulgationVo(type.toVo(DataFilter.get()));
            message.checkCreatePromulgations().add(audio);
        }
    }


    /***************************************/
    /** Generating promulgations          **/
    /***************************************/


    /** {@inheritDoc} */
    @Override
    public BaseMessagePromulgationVo generateMessagePromulgation(SystemMessageVo message, PromulgationType type) throws PromulgationException {

        AudioMessagePromulgationVo audio = new AudioMessagePromulgationVo(type.toVo(DataFilter.get()));

        String language = getLanguage(type);
        StringBuilder text = new StringBuilder();
        if (message.getParts() != null) {
            message.getParts().stream()
                    .filter(p -> p.getType() == MessagePartType.DETAILS && p.getDescs() != null)
                    .flatMap(p -> p.getDescs().stream())
                    .filter(d -> d.getLang().equals(language))
                    .filter(d -> StringUtils.isNotBlank(d.getDetails()))
                    .map(d -> html2audio(d.getDetails(), type.getLanguage()))
                    .forEach(d -> text.append(d).append(System.lineSeparator()));
        }

        if (text.length() > 0) {
            audio.setPromulgate(true);
            audio.setText(text.toString());
        } else {
            audio.setPromulgate(type.getRequirement() == Requirement.MANDATORY);
        }

        return audio;
    }


    /** Transforms a HTML description to an audio description **/
    private String html2audio(String text, String language) {

        // Convert to plain text
        text = TextUtils.html2txt(text, true);

        // Replace positions with audio versions
        ResourceBundle bundle = dictionaryService.getDictionariesAsResourceBundle(
                new String[]{"template"},
                app.getLanguage(language));

        PositionAssembler audioPosAssembler = PositionAssembler.newAudioPositionAssembler(app.getLocale(language), bundle);
        text = PositionUtils.updatePositionFormat(text, audioPosAssembler);

        // Replace certain standard words, e.g. "pos." with "position"
        text = text.replaceAll("(?is)\\s+(pos\\.)\\s+", " position ");

        return text;
    }


    /** {@inheritDoc} */
    @Override
    public void messagePromulgationGenerated(SystemMessageVo message, PromulgationType type) throws PromulgationException {
        AudioMessagePromulgationVo audio = message.promulgation(AudioMessagePromulgationVo.class, type.getTypeId());
        if (audio != null && StringUtils.isNotBlank(audio.getText())) {

            String text = audio.getText()
                    .replaceAll("(?s)\\n+", "\n")
                    .trim();

            audio.setText(text);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void resetMessagePromulgation(SystemMessageVo message, PromulgationType type) throws PromulgationException {
        AudioMessagePromulgationVo audio = message.promulgation(AudioMessagePromulgationVo.class, type.getTypeId());
        if (audio != null) {
            audio.reset();
        }
    }
}
