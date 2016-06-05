package org.mcp.hibernate.envers.query.internal.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.configuration.internal.AuditEntitiesConfiguration;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.internal.entities.mapper.relation.query.QueryConstants;
import org.hibernate.envers.internal.reader.AuditReaderImplementor;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.internal.impl.RevisionsOfEntityQuery;
import org.hibernate.proxy.HibernateProxy;

/**
 * <p>
 * Query similar to {@link RevisionsOfEntityQuery} but always returns list of arrays
 * (as {@link RevisionsOfEntityQuery} with selectEntitiesOnly == false does), and
 * for revisions of type {@link RevisionType#MOD} returns set of modified properties.
 * </p>
 * 
 * @author Szczepan Kuzniarz
 */
public class ExtRevisionsOfEntityQuery extends RevisionsOfEntityQuery {
	/**
	 * Private in superclass
	 */
	protected boolean selectDeletedEntities;
	
	/**
	 * @see RevisionsOfEntityQuery#RevisionsOfEntityQuery(AuditConfiguration, AuditReaderImplementor, Class, boolean, boolean)
	 */
	public ExtRevisionsOfEntityQuery(EnversService enversService, AuditReaderImplementor versionsReader, Class<?> cls, boolean selectDeletedEntities) {
		super(enversService, versionsReader, cls, false, selectDeletedEntities);
		this.selectDeletedEntities = selectDeletedEntities;
	}

	/**
	 * <p>
	 * Very similar to {@link RevisionsOfEntityQuery#list()}, but returns always a list of Object[] and
	 * when the revision type is MOD fourth element of Object array is a set of changed properties. 
	 * </p>
	 */
	@SuppressWarnings("rawtypes")
	@Override
    public List<Object> list() throws AuditException {
		AuditEntitiesConfiguration verEntCfg = enversService.getAuditEntitiesConfiguration();

        /*
		The query that should be executed in the versions table:
        SELECT e (unless another projection is specified) FROM ent_ver e, rev_entity r WHERE
          e.revision_type != DEL (if selectDeletedEntities == false) AND
          e.revision = r.revision AND
          (all specified conditions, transformed, on the "e" entity)
          ORDER BY e.revision ASC (unless another order or projection is specified)
         */
		if ( !selectDeletedEntities ) {
			// e.revision_type != DEL AND
			qb.getRootParameters().addWhereWithParam( verEntCfg.getRevisionTypePropName(), "<>", RevisionType.DEL );
		}

		// all specified conditions, transformed
		for ( AuditCriterion criterion : criterions ) {
			criterion.addToQuery(
					enversService,
					versionsReader,
					aliasToEntityNameMap,
					QueryConstants.REFERENCED_ENTITY_ALIAS,
					qb,
					qb.getRootParameters()
			);
		}

		if ( !hasProjection() && !hasOrder ) {
            String revisionPropertyPath = verEntCfg.getRevisionNumberPath();
			qb.addOrder( QueryConstants.REFERENCED_ENTITY_ALIAS, revisionPropertyPath, true );
        }

		qb.addFrom( enversService.getAuditEntitiesConfiguration().getRevisionInfoEntityName(), "r", true );
		qb.getRootParameters().addWhere(
				enversService.getAuditEntitiesConfiguration().getRevisionNumberPath(),
				true,
				"=",
				"r.id",
				false
		);

        @SuppressWarnings("unchecked")
		List<Object> queryResult = buildAndExecuteQuery();
		if ( hasProjection() ) {
            return queryResult;
        } else {
			List<Object> entities = new ArrayList<Object>();
			String revisionTypePropertyName = verEntCfg.getRevisionTypePropName();

			for ( Object resultRow : queryResult ) {
				Map versionsEntity;
				Object revisionData;

				Object[] arrayResultRow = (Object[]) resultRow;
				versionsEntity = (Map) arrayResultRow[0];
				revisionData = arrayResultRow[1];

                Number revision = getRevisionNumber(versionsEntity);
                
				Object entity = entityInstantiator.createInstanceFromVersionsEntity(
						entityName,
						versionsEntity,
						revision
				);
				
                RevisionType revisionType = (RevisionType) versionsEntity.get(revisionTypePropertyName);
                if (revisionType == RevisionType.MOD) {
                    entities.add(new Object[] { entity, revisionData, revisionType, getChangedProperties(versionsEntity) });                	
                } else {
                    entities.add(new Object[] { entity, revisionData, revisionType });
                }
            }

            return entities;
        }
    }
	
	/**
	 * <p>
	 * Exact copy from superclass. We need this method in {@link #list()} above, but it is
	 * private in {@link RevisionsOfEntityQuery}...
	 * </p>
	 * 
	 * @see RevisionsOfEntityQuery#getRevisionNumber(Map)
	 */
    @SuppressWarnings("rawtypes")
	protected Number getRevisionNumber(Map versionsEntity) {
        AuditEntitiesConfiguration verEntCfg = enversService.getAuditEntitiesConfiguration();

        String originalId = verEntCfg.getOriginalIdPropName();
        String revisionPropertyName = verEntCfg.getRevisionFieldName();

        Object revisionInfoObject = ((Map) versionsEntity.get(originalId)).get(revisionPropertyName);

        if (revisionInfoObject instanceof HibernateProxy) {
            return (Number) ((HibernateProxy) revisionInfoObject).getHibernateLazyInitializer().getIdentifier();
        } else {
            // Not a proxy - must be read from cache or with a join
            return enversService.getRevisionInfoNumberReader().getRevisionNumber(revisionInfoObject);   
        }
    }
    
    /**
     * <p>
     * Scans the map created by this query execution and returns ale the properties
     * with modification flag set to true.
     * </p>
     * 
     * @param versionsEntity
     * 
     * @return
     */
    protected Set<String> getChangedProperties(@SuppressWarnings("rawtypes") Map versionsEntity) {
    	Set<String> changedProperties = new HashSet<String>();
    	String modifiedFlagSuffix = enversService.getGlobalConfiguration().getModifiedFlagSuffix();
    	for (Object key : versionsEntity.keySet()) {
    		// FIXME maybe all the keys are Strings and we can simply cast?
    		String keyString = key.toString();
    		if (keyString.endsWith(modifiedFlagSuffix)) {
    			Object value = versionsEntity.get(key);
    			if ((value != null) && Boolean.parseBoolean(value.toString())) {
    				changedProperties.add(keyString.substring(0, keyString.length() - modifiedFlagSuffix.length()));
    			}
    		}
    	}
    	return changedProperties;
    }
}
