import TSGesuchsteller from './TSGesuchsteller';
import TSKindContainer from './TSKindContainer';
import TSAbstractAntragEntity from './TSAbstractAntragEntity';
import TSFamiliensituation from './TSFamiliensituation';
import TSEinkommensverschlechterungInfo from './TSEinkommensverschlechterungInfo';
import TSMutationsdaten from './TSMutationsdaten';
import {TSAntragTyp} from './enums/TSAntragTyp';

export default class TSGesuch extends TSAbstractAntragEntity {

    private _gesuchsteller1: TSGesuchsteller;
    private _gesuchsteller2: TSGesuchsteller;
    private _kindContainers: Array<TSKindContainer>;
    private _familiensituation: TSFamiliensituation;
    private _einkommensverschlechterungInfo: TSEinkommensverschlechterungInfo;
    private _mutationsdaten: TSMutationsdaten;
    private _bemerkungen: string;
    private _laufnummer: number;


    public get gesuchsteller1(): TSGesuchsteller {
        return this._gesuchsteller1;
    }

    public set gesuchsteller1(value: TSGesuchsteller) {
        this._gesuchsteller1 = value;
    }

    public get gesuchsteller2(): TSGesuchsteller {
        return this._gesuchsteller2;
    }

    public set gesuchsteller2(value: TSGesuchsteller) {
        this._gesuchsteller2 = value;
    }

    get kindContainers(): Array<TSKindContainer> {
        return this._kindContainers;
    }

    set kindContainers(value: Array<TSKindContainer>) {
        this._kindContainers = value;
    }

    get familiensituation(): TSFamiliensituation {
        return this._familiensituation;
    }

    set familiensituation(value: TSFamiliensituation) {
        this._familiensituation = value;
    }

    get einkommensverschlechterungInfo(): TSEinkommensverschlechterungInfo {
        return this._einkommensverschlechterungInfo;
    }

    set einkommensverschlechterungInfo(value: TSEinkommensverschlechterungInfo) {
        this._einkommensverschlechterungInfo = value;
    }

    get mutationsdaten(): TSMutationsdaten {
        return this._mutationsdaten;
    }

    set mutationsdaten(value: TSMutationsdaten) {
        this._mutationsdaten = value;
    }

    get bemerkungen(): string {
        return this._bemerkungen;
    }

    set bemerkungen(value: string) {
        this._bemerkungen = value;
    }

    get laufnummer(): number {
        return this._laufnummer;
    }

    set laufnummer(value: number) {
        this._laufnummer = value;
    }

    public isMutation(): boolean {
        return this.typ === TSAntragTyp.MUTATION;
    }

    /**
     * Schaut ob der GS1 oder der GS2 mindestens eine umzugsadresse hat
     */
    public isThereAnyUmzug(): boolean {
        if (this.gesuchsteller1 && this.gesuchsteller1.getUmzugAdressen().length > 0) {
            return true;
        }
        if (this.gesuchsteller2 && this.gesuchsteller2.getUmzugAdressen().length > 0) {
            return true;
        }
        return false;
    }
}
