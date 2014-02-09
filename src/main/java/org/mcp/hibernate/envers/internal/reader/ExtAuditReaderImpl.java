package org.mcp.hibernate.envers.internal.reader;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.internal.reader.AuditReaderImpl;
import org.mcp.hibernate.envers.ExtAuditReader;
import org.mcp.hibernate.envers.query.ExtAuditQueryCreator;

public class ExtAuditReaderImpl extends AuditReaderImpl implements ExtAuditReader {
	protected AuditConfiguration auditConfiguration;
	
	public ExtAuditReaderImpl(AuditConfiguration verCfg, Session session, SessionImplementor sessionImplementor) {
		super(verCfg, session, sessionImplementor);
		this.auditConfiguration = verCfg;
	}

	public ExtAuditQueryCreator createQuery() {
		return new ExtAuditQueryCreator(auditConfiguration, this);
	}
}
