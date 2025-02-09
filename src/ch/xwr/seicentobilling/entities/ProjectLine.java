package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;

/**
 * ProjectLine
 */
@DAO(daoClass = ProjectLineDAO.class)
@Caption("{%prlText}")
@Entity
@Table(name = "ProjectLine", schema = "dbo")
public class ProjectLine implements java.io.Serializable {

	private Long prlId;
	private Periode periode;
	private Project project;
	private Date prlReportDate;
	private Double prlHours;
	private String prlText;
	private Long prlitmId;
	private LovState.WorkType prlWorkType;
	private Double prlRate;
	private LovState.State prlState;

	public ProjectLine() {
	}

	@Caption("PrlId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "prlId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getPrlId() {
		return this.prlId;
	}

	public void setPrlId(final Long prlId) {
		this.prlId = prlId;
	}

	@Caption("Periode")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prlperId", nullable = false, columnDefinition = "bigint")
	public Periode getPeriode() {
		return this.periode;
	}

	public void setPeriode(final Periode periode) {
		this.periode = periode;
	}

	@Caption("Project")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prlproId", nullable = false, columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	@Caption("PrlReportDate")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "prlReportDate", nullable = false, columnDefinition = "datetime", length = 23)
	public Date getPrlReportDate() {
		return this.prlReportDate;
	}

	public void setPrlReportDate(final Date prlReportDate) {
		this.prlReportDate = prlReportDate;
	}

	@Caption("PrlHours")
	@Column(name = "prlHours", columnDefinition = "decimal", precision = 6)
	public Double getPrlHours() {
		return this.prlHours;
	}

	public void setPrlHours(final Double prlHours) {
		this.prlHours = prlHours;
	}

	@Caption("PrlText")
	@Column(name = "prlText", columnDefinition = "nvarchar")
	public String getPrlText() {
		return this.prlText;
	}

	public void setPrlText(final String prlText) {
		this.prlText = prlText;
	}

	@Caption("PrlitmId")
	@Column(name = "prlitmId", columnDefinition = "bigint")
	public Long getPrlitmId() {
		return this.prlitmId;
	}

	public void setPrlitmId(final Long prlitmId) {
		this.prlitmId = prlitmId;
	}

	@Caption("PrlWorkType")
	@Column(name = "prlWorkType", columnDefinition = "smallint")
	public LovState.WorkType getPrlWorkType() {
		return this.prlWorkType;
	}

	public void setPrlWorkType(final LovState.WorkType prlWorkType) {
		this.prlWorkType = prlWorkType;
	}

	@Caption("PrlRate")
	@Column(name = "prlRate", columnDefinition = "decimal", precision = 6)
	public Double getPrlRate() {
		return this.prlRate;
	}

	public void setPrlRate(final Double prlRate) {
		this.prlRate = prlRate;
	}

	@Caption("PrlState")
	@Column(name = "prlState", columnDefinition = "smallint")
	public LovState.State getPrlState() {
		return this.prlState;
	}

	public void setPrlState(final LovState.State prlState) {
		this.prlState = prlState;
	}

}
