
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLine_;

/**
 * Home object for domain model class ProjectLine.
 *
 * @see ProjectLine
 */
public class ProjectLineDAO extends JPADAO<ProjectLine, Long> {
	public ProjectLineDAO() {
		super(ProjectLine.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<ProjectLine> findByPeriode(final Periode dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Periode> daoParameter = criteriaBuilder.parameter(Periode.class, "dao");

		final CriteriaQuery<ProjectLine> criteriaQuery = criteriaBuilder.createQuery(ProjectLine.class);

		final Root<ProjectLine> root = criteriaQuery.from(ProjectLine.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(ProjectLine_.periode), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(ProjectLine_.prlReportDate)));

		final TypedQuery<ProjectLine> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}
}