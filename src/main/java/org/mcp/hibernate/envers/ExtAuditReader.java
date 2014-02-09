package org.mcp.hibernate.envers;

import org.hibernate.envers.AuditReader;
import org.mcp.hibernate.envers.query.ExtAuditQueryCreator;

public interface ExtAuditReader extends AuditReader {
	ExtAuditQueryCreator createQuery();
}
