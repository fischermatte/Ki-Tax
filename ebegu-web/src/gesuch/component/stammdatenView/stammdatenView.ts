import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {IComponentOptions} from 'angular';
import AbstractGesuchViewController from '../abstractGesuchView';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {IStammdatenStateParams} from '../../gesuch.route';
import './stammdatenView.less';
import GesuchModelManager from '../../service/gesuchModelManager';
import TSGesuchsteller from '../../../models/TSGesuchsteller';
import BerechnungsManager from '../../service/berechnungsManager';
import ErrorService from '../../../core/errors/service/ErrorService';
import {TSRole} from '../../../models/enums/TSRole';
import WizardStepManager from '../../service/wizardStepManager';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
let template = require('./stammdatenView.html');
require('./stammdatenView.less');

export class StammdatenViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {};
    template = template;
    controller = StammdatenViewController;
    controllerAs = 'vm';
}


export class StammdatenViewController extends AbstractGesuchViewController {
    geschlechter: Array<string>;
    showKorrespondadr: boolean;
    ebeguRestUtil: EbeguRestUtil;
    allowedRoles: Array<TSRole>;


    static $inject = ['$stateParams', 'EbeguRestUtil', 'GesuchModelManager', 'BerechnungsManager', 'ErrorService', 'WizardStepManager',
        'CONSTANTS'];
    /* @ngInject */
    constructor($stateParams: IStammdatenStateParams, ebeguRestUtil: EbeguRestUtil, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private errorService: ErrorService,
                wizardStepManager: WizardStepManager, private CONSTANTS: any) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager);
        this.ebeguRestUtil = ebeguRestUtil;
        let parsedNum: number = parseInt($stateParams.gesuchstellerNumber, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedNum);
        this.initViewmodel();
    }

    private initViewmodel() {
        this.gesuchModelManager.initStammdaten();
        this.wizardStepManager.setCurrentStep(TSWizardStepName.GESUCHSTELLER);
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.showKorrespondadr = (this.gesuchModelManager.getStammdatenToWorkWith().korrespondenzAdresse) ? true : false;
        this.gesuchModelManager.setShowUmzug();
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    korrespondenzAdrClicked() {
        this.gesuchModelManager.setKorrespondenzAdresse(this.showKorrespondadr);
    }

    private save(form: angular.IFormController) {
        if (form.$valid) {
            if (!this.showKorrespondadr) {
                this.gesuchModelManager.setKorrespondenzAdresse(this.showKorrespondadr);
            }
            if (this.gesuchModelManager.showUmzug || this.isMutation()) {
                this.wizardStepManager.unhideStep(TSWizardStepName.UMZUG);
            } else {
                this.wizardStepManager.hideStep(TSWizardStepName.UMZUG);
            }
            this.errorService.clearAll();
            return this.gesuchModelManager.updateGesuchsteller();
        }
        return undefined;
    }

    public getModel(): TSGesuchsteller {
        return this.gesuchModelManager.getStammdatenToWorkWith();
    }

    public isMutation(): boolean {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().isMutation();
        }
        return false;
    }

    /**
     * Die Wohnadresse des GS2 darf bei Mutationen in denen der GS2 bereits existiert, nicht geaendert werden.
     * Die Wohnadresse des GS1 darf bei Mutationen nie geaendert werden
     * @returns {boolean}
     */
    public disableWohnadresseFor2GS(): boolean {
        return this.isMutation() && (this.gesuchModelManager.getGesuchstellerNumber() === 1
            || (this.gesuchModelManager.getStammdatenToWorkWith().vorgaengerId !== null
                && this.gesuchModelManager.getStammdatenToWorkWith().vorgaengerId !== undefined));
    }

    public isThereAnyUmzug(): boolean {
        return this.gesuchModelManager.getGesuch().isThereAnyUmzug();
    }

}
