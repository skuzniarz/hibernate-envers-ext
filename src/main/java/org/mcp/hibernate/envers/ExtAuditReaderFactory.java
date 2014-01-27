package org.mcp.hibernate.envers;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.event.spi.EnversListener;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEventListener;
import org.mcp.hibernate.envers.internal.reader.ExtAuditReaderImpl;

/**
 * <p>
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
		if (!(session instanceof SessionImplementor)) {
			sessionImpl = (SessionImplementor) session.getSessionFactory().getCurrentSession();
		} else {
			sessionImpl = (SessionImplementor) session;
		}

		// todo : I wonder if there is a better means to do this via "named lookup" based on the session factory name/uuid
		final EventListenerRegistry listenerRegistry = sessionImpl
				.getFactory()
				.getServiceRegistry()
				.getService( EventListenerRegistry.class );

		for ( PostInsertEventListener listener : listenerRegistry.getEventListenerGroup( EventType.POST_INSERT )
				.listeners() ) {
			if ( listener instanceof EnversListener ) {
				// todo : slightly different from original code in that I am not checking the other listener groups...
				return new ExtAuditReaderImpl(
						((EnversListener) listener).getAuditConfiguration(),
						session,
						sessionImpl
				);
			}
		}

        throw new AuditException( "Envers listeners were not properly registered" );
    }

    /**
     * <p>
     * Exact copy of {@link AuditReaderFactory#get(EntityManager)}, but it is impossible to inherit
     * from {@link AuditReaderFactory}.
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
