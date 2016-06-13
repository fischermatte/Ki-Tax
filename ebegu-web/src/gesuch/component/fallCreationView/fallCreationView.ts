import {IComponentOptions, IFormController} from 'angular';
import {IStateService} from 'angular-ui-router';
import AbstractGesuchViewController from '../abstractGesuchView';
import GesuchModelManager from '../../service/gesuchModelManager';
import BerechnungsManager from '../../service/berechnungsManager';
import TSGesuch from '../../../models/TSGesuch';
import EbeguUtil from '../../../utils/EbeguUtil';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
let template = require('./fallCreationView.html');
require('./fallCreationView.less');

export class FallCreationViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = FallCreationViewController;
    controllerAs = 'vm';
}

export class FallCreationViewController extends AbstractGesuchViewController {
    private gesuchsperiodeId: string;

    static $inject = ['$state', 'GesuchModelManager', 'BerechnungsManager', 'EbeguUtil'];
    /* @ngInject */
    constructor(state: IStateService, gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager, private ebeguUtil: EbeguUtil) {
        super(state, gesuchModelManager, berechnungsManager);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.gesuchModelManager.initGesuch();
        if (this.gesuchModelManager.getGesuchsperiode()) {
            this.gesuchsperiodeId = this.gesuchModelManager.getGesuchsperiode().id;
        }
    }

    public getGesuchModel(): TSGesuch {
        return this.gesuchModelManager.gesuch;
    }

    submit(form: IFormController) {
        if (form.$valid) {
            this.gesuchModelManager.saveGesuchAndFall().then((response: any) => {
                this.state.go('gesuch.familiensituation');
            });
        }
    }

    public getGesuchsperiodeAsString(gesuchsperiode: TSGesuchsperiode): string {
        return this.ebeguUtil.getGesuchsperiodeAsString(gesuchsperiode);
    }

    /**
     * Calls getGesuchsperiodeAsString with the Gesuchsperiode of the current Gesuch
     * @returns {string}
     */
    public getCurrentGesuchsperiodeAsString(): string {
        return this.ebeguUtil.getGesuchsperiodeAsString(this.gesuchModelManager.getGesuchsperiode());
    }

    public getAllActiveGesuchsperioden() {
        return this.gesuchModelManager.getAllActiveGesuchsperioden();
    }

    public setSelectedGesuchsperiode(): void {
        let gesuchsperiodeList = this.getAllActiveGesuchsperioden();
        for (let i: number = 0; i < gesuchsperiodeList.length; i++) {
            if (gesuchsperiodeList[i].id === this.gesuchsperiodeId) {
                this.getGesuchModel().gesuchsperiode = gesuchsperiodeList[i];
            }
        }
    }

}