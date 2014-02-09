package org.mcp.hibernate.envers;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditQueryCreator;
import org.mcp.hibernate.envers.query.ExtAuditQueryCreator;

/**
 * <p>
 * Extension of {@link AuditReader} returning {@link ExtAuditQueryCreator}
 * instead of {@link AuditQueryCreator}.
 * </p>
 * 
 * @author Szczepan Kuzniarz
 */
public interface ExtAuditReader extends AuditReader {
	/**
	 * {@inheritDoc}
	 */
	ExtAuditQueryCreator createQuery();
}
