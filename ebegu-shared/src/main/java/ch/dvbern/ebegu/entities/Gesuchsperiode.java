package ch.dvbern.ebegu.entities;

import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Entity fuer Gesuchsperiode.
 */
@Audited
@Entity
public class Gesuchsperiode extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -9132257370971574570L;

	@NotNull
	@Column(nullable = false)
	private Boolean active;

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@SuppressWarnings({"ObjectEquality", "OverlyComplexBooleanExpression"})
	public boolean isSame(Gesuchsperiode otherGesuchsperiode) {
		boolean dateRangeIsSame = super.isSame(otherGesuchsperiode);
		boolean activeSame = Objects.equals(this.getActive(), otherGesuchsperiode.getActive());
		return dateRangeIsSame && activeSame;
	}
}