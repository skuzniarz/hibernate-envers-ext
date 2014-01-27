package org.mcp.hibernate.envers.internal.reader;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.internal.reader.AuditReaderImpl;
import org.mcp.hibernate.envers.ExtAuditReader;
import org.mcp.hibernate.envers.query.ExtAuditQueryCreator;

/**
 * <p>
 * </p>
 * 
 * @author Szczepan Kuzniarz
 */
public class ExtAuditReaderImpl extends AuditReaderImpl implements ExtAuditReader {
	/**
	 * Private in superclass
	 * 
	 * @see AuditReaderImpl#verCfg
	 */
	protected AuditConfiguration auditConfiguration;
	
	/**
	 * @see AuditReaderImpl#AuditReaderImpl(AuditConfiguration, Session, SessionImplementor)
	 */
	public ExtAuditReaderImpl(AuditConfiguration verCfg, Session session, SessionImplementor sessionImplementor) {
		super(verCfg, session, sessionImplementor);
		this.auditConfiguration = verCfg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtAuditQueryCreator createQuery() {
		return new ExtAuditQueryCreator(auditConfiguration, this);
	}
}
