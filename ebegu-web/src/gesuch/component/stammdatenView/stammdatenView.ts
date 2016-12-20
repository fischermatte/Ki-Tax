import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {IComponentOptions} from 'angular';
import AbstractGesuchViewController from '../abstractGesuchView';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {IStammdatenStateParams} from '../../gesuch.route';
import './stammdatenView.less';
import GesuchModelManager from '../../service/gesuchModelManager';
import BerechnungsManager from '../../service/berechnungsManager';
import ErrorService from '../../../core/errors/service/ErrorService';
import {TSRole} from '../../../models/enums/TSRole';
import WizardStepManager from '../../service/wizardStepManager';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSGesuchstellerContainer from '../../../models/TSGesuchstellerContainer';
import TSAdresseContainer from '../../../models/TSAdresseContainer';
import TSAdresse from '../../../models/TSAdresse';
import {TSAdressetyp} from '../../../models/enums/TSAdressetyp';
import IQService = angular.IQService;
import IPromise = angular.IPromise;
import IScope = angular.IScope;
import ITranslateService = angular.translate.ITranslateService;
let template = require('./stammdatenView.html');
require('./stammdatenView.less');

export class StammdatenViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {};
    template = template;
    controller = StammdatenViewController;
    controllerAs = 'vm';
}


export class StammdatenViewController extends AbstractGesuchViewController<TSGesuchstellerContainer> {
    geschlechter: Array<string>;
    showKorrespondadr: boolean;
    showKorrespondadrGS: boolean;
    ebeguRestUtil: EbeguRestUtil;
    allowedRoles: Array<TSRole>;
    gesuchstellerNumber: number;
    private initialModel: TSGesuchstellerContainer;


    static $inject = ['$stateParams', 'EbeguRestUtil', 'GesuchModelManager', 'BerechnungsManager', 'ErrorService', 'WizardStepManager',
        'CONSTANTS', '$q', '$scope', '$translate'];
    /* @ngInject */
    constructor($stateParams: IStammdatenStateParams, ebeguRestUtil: EbeguRestUtil, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private errorService: ErrorService,
                wizardStepManager: WizardStepManager, private CONSTANTS: any, private $q: IQService, $scope: IScope,
                private $translate: ITranslateService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope);
        this.ebeguRestUtil = ebeguRestUtil;
        this.gesuchstellerNumber = parseInt($stateParams.gesuchstellerNumber, 10);
        this.gesuchModelManager.setGesuchstellerNumber(this.gesuchstellerNumber);
        this.initViewmodel();
    }

    private initViewmodel() {
        this.gesuchModelManager.initStammdaten();
        this.model = angular.copy(this.gesuchModelManager.getStammdatenToWorkWith());
        this.initialModel = angular.copy(this.model);
        this.wizardStepManager.setCurrentStep(TSWizardStepName.GESUCHSTELLER);
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.showKorrespondadr = (this.model.korrespondenzAdresse && this.model.korrespondenzAdresse.adresseJA) ? true : false;
        this.showKorrespondadrGS = (this.model.korrespondenzAdresse && this.model.korrespondenzAdresse.adresseGS) ? true : false;
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.getModel().showUmzug = this.getModel().showUmzug || this.getModel().isThereAnyUmzug();
    }

    korrespondenzAdrClicked() {
        if (this.showKorrespondadr && (!this.model.korrespondenzAdresse || !this.model.korrespondenzAdresse.adresseJA)) {
            this.model.korrespondenzAdresse = this.initKorrespondenzAdresse();
        }
    }

    private save(): IPromise<TSGesuchstellerContainer> {
        if (this.form.$valid) {
            this.gesuchModelManager.setStammdatenToWorkWith(this.model);
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                if (this.gesuchModelManager.getGesuchstellerNumber() === 1 && !this.gesuchModelManager.isGesuchsteller2Required()) {
                    this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
                }
                if (this.gesuchModelManager.getGesuchstellerNumber() === 2) {
                    this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
                }

                return this.$q.when(this.model);
            }
            // wenn keine Korrespondenzaddr da ist koennen wir sie wegmachen
            this.maybeResetKorrespondadr();

            if ((this.gesuchModelManager.getGesuch().gesuchsteller1 && this.gesuchModelManager.getGesuch().gesuchsteller1.showUmzug)
                || (this.gesuchModelManager.getGesuch().gesuchsteller2 && this.gesuchModelManager.getGesuch().gesuchsteller2.showUmzug)
                || this.isMutation()) {
                this.wizardStepManager.unhideStep(TSWizardStepName.UMZUG);
            } else {
                this.wizardStepManager.hideStep(TSWizardStepName.UMZUG);
            }
            this.errorService.clearAll();
            return this.gesuchModelManager.updateGesuchsteller(false);
        }
        return undefined;
    }

    public getModel(): TSGesuchstellerContainer {
        return this.model;
    }

    public getModelJA() {
        return this.model.gesuchstellerJA;
    }

    /**
     * Die Wohnadresse des GS2 darf bei Mutationen in denen der GS2 bereits existiert, nicht geaendert werden.
     * Die Wohnadresse des GS1 darf bei Mutationen nie geaendert werden
     * @returns {boolean}
     */
    public disableWohnadresseFor2GS(): boolean {
        return this.isMutation() && (this.gesuchstellerNumber === 1
            || (this.model.vorgaengerId !== null
            && this.model.vorgaengerId !== undefined));
    }

    public isThereAnyUmzug(): boolean {
        return this.gesuchModelManager.getGesuch().isThereAnyUmzug();
    }

    private maybeResetKorrespondadr(): void {
        if (!this.showKorrespondadr && !this.showKorrespondadrGS) {
            this.getModel().korrespondenzAdresse = undefined; //keine korrAdr weder von GS noch von JA -> entfernen
        } else if (!this.showKorrespondadr) {
            this.getModel().korrespondenzAdresse.adresseJA = undefined; //nur adresse JA wird zurueckgesetzt die GS kann bleiben
        }

    }

    private initKorrespondenzAdresse(): TSAdresseContainer {
        let korrespAdresseContanier: TSAdresseContainer = new TSAdresseContainer();
        let korrAdr = new TSAdresse();
        korrAdr.adresseTyp = TSAdressetyp.KORRESPONDENZADRESSE;
        korrespAdresseContanier.showDatumVon = false;
        korrespAdresseContanier.adresseJA = korrAdr;
        return korrespAdresseContanier;
    }

    public getTextKorrespondenzaddrKorrekturJA(): string {
        if (this.model.korrespondenzAdresse && this.model.korrespondenzAdresse.adresseGS) {
            let adr: TSAdresse = this.model.korrespondenzAdresse.adresseGS;
            let organisation: string = adr.organisation ? adr.organisation : '-';
            let strasse: string = adr.strasse ? adr.strasse : '-';
            let hausnummer: string = adr.hausnummer ? adr.hausnummer : '-';
            let zusatzzeile: string = adr.zusatzzeile ? adr.zusatzzeile : '-';
            let plz: string = adr.plz ? adr.plz : '-';
            let ort: string = adr.ort ? adr.ort : '-';
            let land: string = this.$translate.instant('Land_' + adr.land);
            return this.$translate.instant('JA_KORREKTUR_KORRESPONDENZ_ADDR', {
                organisation: organisation,
                strasse: strasse,
                hausnummer: hausnummer,
                zusatzzeile: zusatzzeile,
                plz: plz,
                ort: ort,
                land: land,
            });
        } else {
            return this.$translate.instant('LABEL_KEINE_ANGABE');
        }

    }

}
