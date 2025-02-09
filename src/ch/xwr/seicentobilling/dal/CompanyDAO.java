
package ch.xwr.seicentobilling.dal;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.entities.Company_;

/**
 * Home object for domain model class Company.
 *
 * @see Company
 */
public class CompanyDAO extends JPADAO<Company, Long> {
	public CompanyDAO() {
		super(Company.class);
	}

	public Company getActiveConfig()
	{
		final List<Company> lst = findActiveConfig(LovState.State.active);
		//List<Company> lst = findActiveConfig((short) 1);

		return lst.get(0);
	}

	public String getDbNameNativeSQL()
	{
		final org.hibernate.engine.spi.SessionImplementor sessionImp =
		     (org.hibernate.engine.spi.SessionImplementor) em().getDelegate();
		//do whatever you need with the metadata object...
		try {
			final DatabaseMetaData metadata = sessionImp.connection().getMetaData();

			String cleanURI = metadata.getURL().toString().substring(5);  //removev jdbc:
			final int pos1 = cleanURI.indexOf(';');
			cleanURI = cleanURI.substring(12, (pos1-5));

			return cleanURI;
		} catch (final SQLException e) {
			e.printStackTrace();
		}

	    return "unknown";
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Company> findActiveConfig(final Enum state) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Enum> stateParameter = criteriaBuilder.parameter(Enum.class, "state");

		final CriteriaQuery<Company> criteriaQuery = criteriaBuilder.createQuery(Company.class);

		final Root<Company> root = criteriaQuery.from(Company.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Company_.cmpState), stateParameter));

		final TypedQuery<Company> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(stateParameter, state);
		return query.getResultList();
	}
}