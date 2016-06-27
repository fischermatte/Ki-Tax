import {IWindowService, IHttpInterceptor, IRootScopeService, IQService} from 'angular';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import HttpBuffer from './HttpBuffer';

export default class HttpAuthInterceptor implements IHttpInterceptor {

    static $inject = ['$rootScope', '$q', 'CONSTANTS', '$window', 'httpBuffer'];
    /* @ngInject */
    constructor(private $rootScope: IRootScopeService, private $q: IQService, private CONSTANTS: any,
                private $window: IWindowService, private httpBuffer: HttpBuffer) {
    }


    public responseError = (response: any) => {
        switch (response.status) {
            case 401:
                // exclude requests from the login form
                if (response.config && response.config.url === this.CONSTANTS.REST_API + '/api/v1/auth/login') {
                    return this.$q.reject(response);
                }
                // all requests that failed due to notAuthenticated are appended to httpBuffer. Use httpBuffer.retryAll to submit them.
                let deferred = this.$q.defer();
                this.httpBuffer.append(response.config, deferred);
                this.$rootScope.$broadcast(TSAuthEvent[TSAuthEvent.NOT_AUTHENTICATED], response);
                this.$window.location.href = '/#/src/authentication/dummyAuthentication.html';
                return deferred.promise;
        }
        return this.$q.reject(response);
    };
}