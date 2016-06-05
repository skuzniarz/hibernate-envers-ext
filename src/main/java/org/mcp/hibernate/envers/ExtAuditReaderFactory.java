package org.mcp.hibernate.envers;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.service.ServiceRegistry;
import org.mcp.hibernate.envers.internal.reader.ExtAuditReaderImpl;

/**
 * <p>
 * Factory for {@link ExtAuditReader} instances, works just like {@link AuditReaderFactory}.
 * </p>
 * 
 * @author Szczepan Kuzniarz
 */
public class ExtAuditReaderFactory {
    /**
     * <p>
     * The same logic as in {@link AuditReaderFactory#get(Session)}, but returns instance of {@link ExtAuditReaderImpl}.
     * </p>
     * 
     * @see AuditReaderFactory#get(Session)
     */
    public static ExtAuditReader get(Session session) throws AuditException {
		SessionImplementor sessionImpl;
		if ( !(session instanceof SessionImplementor) ) {
			sessionImpl = (SessionImplementor) session.getSessionFactory().getCurrentSession();
		}
		else {
			sessionImpl = (SessionImplementor) session;
		}

		final ServiceRegistry serviceRegistry = sessionImpl.getFactory().getServiceRegistry();
		final EnversService enversService = serviceRegistry.getService( EnversService.class );

		return new ExtAuditReaderImpl( enversService, session, sessionImpl );
	}

    /**
     * <p>
     * Exact copy of {@link AuditReaderFactory#get(EntityManager)}.
     * </p>
     */
    public static ExtAuditReader get(EntityManager entityManager) throws AuditException {
        if (entityManager.getDelegate() instanceof Session) {
            return get((Session) entityManager.getDelegate());
        }

        if (entityManager.getDelegate() instanceof EntityManager) {
            return get((EntityManager) entityManager.getDelegate());
        }

        throw new AuditException("Hibernate EntityManager not present!");
    }
}
