
/**
 * The site admin functionality.
 */
angular.module('niord.admin')

    /**
     * Legacy NW import Controller
     */
    .controller('NwIntegrationCtrl', ['$scope', '$rootScope', '$http', 'growl', 'MessageService',
        function ($scope, $rootScope, $http, growl, MessageService) {
            'use strict';

            $scope.legacyNwResult = '';

            /** Displays the error message */
            $scope.displayError = function (err) {
                growl.error("Error");
                $scope.legacyNwResult = 'Error:\n' + err;
            };

            $scope.data = {
                seriesId: undefined,
                tagName: ''
            };
            $scope.tagData = {
                tag: undefined
            };

            // Load the default parameters
            $http.get('/rest/import/nw/params')
                .success(function (result) {
                    $scope.data = result;

                    $scope.tagData.tag = undefined;
                    if (result && result.tagName) {
                        $http.get('/rest/tags/tag/' + result.tagName).then(function(response) {
                            if (response.data && response.data.length > 0) {
                                $scope.tagData.tag = response.data[0];
                            }
                        });
                    }
                });


            /** Tests the legacy NW database connection */
            $scope.testConnection = function() {
                $scope.legacyNwResult = 'Trying to connect...';
                $http.get('/rest/import/nw/test-connection')
                    .success(function (result) {
                        $scope.legacyNwResult = 'Connection status: ' + result;
                    })
                    .error($scope.displayError);

            };

            // Determine the message series for the current domain
            $scope.messageSeriesIds = [];
            if ($rootScope.domain && $rootScope.domain.messageSeries) {
                angular.forEach($rootScope.domain.messageSeries, function (series) {
                    if (series.mainType == 'NW') {
                        $scope.messageSeriesIds.push(series.seriesId);
                    }
                });
            }


            /** Refreshes the tags search result */
            $scope.tags = [];
            $scope.refreshTags = function(name) {
                if (!name || name.length == 0) {
                    return [];
                }
                return $http.get(
                    '/rest/tags/search?name=' + encodeURIComponent(name) + '&limit=10'
                ).then(function(response) {
                    $scope.tags = response.data;
                });
            };


            /** Opens the tags dialog */
            $scope.openTagsDialog = function () {
                MessageService.messageTagsDialog().result
                    .then(function (tag) {
                        $scope.tagData.tag = tag;
                    });
            };


            /** Removes the current tag selection */
            $scope.removeTag = function () {
                $scope.tagData.tag = undefined;
            };

            // Sync the tagData.tag with the data.tagName
            $scope.$watch("tagData", function () {
                $scope.data.tagName = $scope.tagData.tag ? $scope.tagData.tag.tagId : undefined;
            }, true);


            /** Imports the legacy NW messages */
            $scope.importLegacyNw = function () {
                $scope.legacyNwResult = 'Start import of legacy MW messages';

                $http.post('/rest/import/nw/import-nw', $scope.data)
                    .success(function (result) {
                        $scope.legacyNwResult = result;
                    })
                    .error($scope.displayError);
            }

        }])


    /**
     * Legacy NM import Controller
     */
    .controller('NmIntegrationCtrl', ['$scope', '$rootScope', '$http', 'growl', 'MessageService',
        function ($scope, $rootScope, $http, growl, MessageService) {
            'use strict';

            $scope.nmImportUrl = '/rest/import/nm/import-nm';
            $scope.legacyNmResult = '';

            /** Displays the error message */
            $scope.displayError = function (err) {
                growl.error("Error");
                $scope.legacyNmResult = 'Error:\n' + err;
            };


            // Determine the message series for the current domain
            $scope.messageSeriesIds = [];
            if ($rootScope.domain && $rootScope.domain.messageSeries) {
                angular.forEach($rootScope.domain.messageSeries, function (series) {
                    if (series.mainType == 'NM') {
                        $scope.messageSeriesIds.push(series.seriesId);
                    }
                });
            }

            $scope.data = {
                seriesId: $scope.messageSeriesIds.length == 1 ? $scope.messageSeriesIds[0] : undefined,
                tagName: ''
            };


            /** Refreshes the tags search result */
            $scope.tags = [];
            $scope.tagData = { tag: undefined };
            $scope.refreshTags = function(name) {
                if (!name || name.length == 0) {
                    return [];
                }
                return $http.get(
                    '/rest/tags/search?name=' + encodeURIComponent(name) + '&limit=10'
                ).then(function(response) {
                    $scope.tags = response.data;
                });
            };

            /** Opens the tags dialog */
            $scope.openTagsDialog = function () {
                MessageService.messageTagsDialog().result
                    .then(function (tag) {
                        if (tag) {
                            $scope.tagData.tag = tag;
                        }
                    });
            };

            /** Removes the current tag selection */
            $scope.removeTag = function () {
                $scope.tagData.tag = undefined;
            };


            // Sync the tagData.tag with the data.tagName
            $scope.$watch("tagData", function () {
                $scope.data.tagName = $scope.tagData.tag ? $scope.tagData.tag.tagId : undefined;
            }, true);


            /** Called when the NM html file has been imported */
            $scope.nmFileUploaded = function(result) {
                $scope.legacyNmResult = result;
                $scope.$$phase || $scope.$apply();
            };

            /** Called when the NM html import has failed */
            $scope.nmFileUploadError = function(status, statusText) {
                $scope.legacyNmResult = "Error importing NMs (error " + status + ")";
                $scope.$$phase || $scope.$apply();
            };

        }])


    /**
     * Aton Import Controller
     */
    .controller('AtonIntegrationCtrl', ['$scope',
        function ($scope) {
            'use strict';

            $scope.atonUploadUrl = '/rest/import/atons/upload-xls';
            $scope.importResult = '';

            $scope.xlsFileUploaded = function(result) {
                $scope.importResult = result;
                $scope.$$phase || $scope.$apply();
            };

            $scope.xlsFileUploadError = function(status, statusText) {
                $scope.importResult = "Error importing AtoNs (error " + status + ")";
                $scope.$$phase || $scope.$apply();
            };

        }]);
