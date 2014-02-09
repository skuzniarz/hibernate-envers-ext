package org.mcp.hibernate.envers.query;

import static org.hibernate.envers.internal.tools.EntityTools.getTargetClassIfProxied;

import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.internal.reader.AuditReaderImplementor;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.mcp.hibernate.envers.query.internal.impl.ExtRevisionsOfEntityQuery;

public class ExtAuditQueryCreator extends AuditQueryCreator {
    protected AuditConfiguration auditConfiguration;
	
    protected AuditReaderImplementor auditReaderImplementor;
	
    public ExtAuditQueryCreator(AuditConfiguration auditCfg,
            AuditReaderImplementor auditReaderImplementor) {
		
        super(auditCfg, auditReaderImplementor);
        this.auditConfiguration = auditCfg;
        this.auditReaderImplementor = auditReaderImplementor;
	}
	
    public AuditQuery forRevisionsOfEntityAndChanges(Class<?> c,
            boolean selectDeletedEntities) {
        
        c = getTargetClassIfProxied(c);
        return new ExtRevisionsOfEntityQuery(
            auditConfiguration,
            auditReaderImplementor,
            c,
            selectDeletedEntities
        );
    }
}
