package org.mcp.hibernate.envers;

import org.hibernate.envers.AuditReader;
import org.mcp.hibernate.envers.query.ExtAuditQueryCreator;

/**
 * <p>
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
