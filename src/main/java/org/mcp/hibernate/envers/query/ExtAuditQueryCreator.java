package org.mcp.hibernate.envers.query;

import static org.hibernate.envers.tools.Tools.getTargetClassIfProxied;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.envers.reader.AuditReaderImplementor;
import org.mcp.hibernate.envers.query.impl.ExtRevisionsOfEntityQuery;

/**
 * <p>
 * </p>
 * 
 * @author Szczepan Kuzniarz
 */
public class ExtAuditQueryCreator extends AuditQueryCreator {
	/**
	 * Private in superclass
	 * 
	 * @see AuditQueryCreator#auditCfg
	 */
	protected AuditConfiguration auditConfiguration;
	
	/**
	 * Private in superclass
	 * 
	 * @see AuditQueryCreator#auditReaderImplementor
	 */
	protected AuditReaderImplementor auditReaderImplementor;
	
	/**
	 * @see AuditQueryCreator#AuditQueryCreator(AuditConfiguration, AuditReaderImplementor)
	 */
	public ExtAuditQueryCreator(AuditConfiguration auditCfg, AuditReaderImplementor auditReaderImplementor) {
		super(auditCfg, auditReaderImplementor);
		this.auditConfiguration = auditCfg;
		this.auditReaderImplementor = auditReaderImplementor;
	}
	
	/**
	 * <p>
	 * Creates a query, which selects the revisions, at which the given entity was modified.
     * The result will be a list of Object[] containing:
     * <ol>
     * <li>the entity instance</li>
     * <li>revision entity, corresponding to the revision at which the entity was modified</li>
     * <li>type of the revision (instance of {@link org.hibernate.envers.RevisionType})</li>
     * <li>if type of the revision is {@link RevisionType#MOD} set of modified attributes names</li>
     * </ol> 
	 * </p>
	 * 
	 * @param c
	 * @param selectDeletedEntities
	 * 
	 * @return list of Object[]
	 * 
	 * @see AuditQueryCreator#forRevisionsOfEntity(Class, boolean, boolean)
	 */
    public AuditQuery forRevisionsOfEntityAndChanges(Class<?> c, boolean selectDeletedEntities) {
        c = getTargetClassIfProxied(c);
        return new ExtRevisionsOfEntityQuery(auditConfiguration, auditReaderImplementor, c, selectDeletedEntities);
    }
}
