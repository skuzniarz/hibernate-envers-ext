package org.mcp.hibernate.envers;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.event.spi.EnversListener;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEventListener;
import org.mcp.hibernate.envers.internal.reader.ExtAuditReaderImpl;

public class ExtAuditReaderFactory {
    public static ExtAuditReader get(Session session) throws AuditException {
        SessionImplementor sessionImpl;
		if (!(session instanceof SessionImplementor)) {
			sessionImpl = (SessionImplementor) session.getSessionFactory()
					.getCurrentSession();
		} else {
			sessionImpl = (SessionImplementor) session;
		}

		final EventListenerRegistry listenerRegistry = sessionImpl
				.getFactory()
				.getServiceRegistry()
				.getService( EventListenerRegistry.class );

		for ( PostInsertEventListener listener : listenerRegistry
				.getEventListenerGroup( EventType.POST_INSERT ).listeners() ) {
			if ( listener instanceof EnversListener ) {
				return new ExtAuditReaderImpl(
						((EnversListener) listener).getAuditConfiguration(),
						session,
						sessionImpl
				);
			}
		}
        throw new AuditException( "Envers listeners were not properly registered" );
    }

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
