package org.mcp.hibernate.envers.query.internal.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.configuration.internal.AuditEntitiesConfiguration;
import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.internal.reader.AuditReaderImplementor;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.internal.impl.RevisionsOfEntityQuery;
import org.hibernate.proxy.HibernateProxy;

public class ExtRevisionsOfEntityQuery extends RevisionsOfEntityQuery {
	protected boolean selectDeletedEntities;
	
	public ExtRevisionsOfEntityQuery(AuditConfiguration verCfg,
			AuditReaderImplementor versionsReader, Class<?> cls,
			boolean selectDeletedEntities) {
		
		super( verCfg, versionsReader, cls, false, selectDeletedEntities );
		this.selectDeletedEntities = selectDeletedEntities;
	}

    public List<Object> list() throws AuditException {
        AuditEntitiesConfiguration verEntCfg = verCfg.getAuditEntCfg();

        if ( !selectDeletedEntities) {
            qb.getRootParameters().addWhereWithParam(
            		verEntCfg.getRevisionTypePropName(),
            		"<>",
            		RevisionType.DEL
            );
        }

        for ( AuditCriterion criterion : criterions ) {
            criterion.addToQuery(
            		verCfg,
            		versionsReader,
            		entityName,
            		qb,
            		qb.getRootParameters()
            );
        }

        if ( !hasProjection && !hasOrder ) {
            String revisionPropertyPath = verEntCfg.getRevisionNumberPath();
            qb.addOrder( revisionPropertyPath, true );
        }

        qb.addFrom( verCfg.getAuditEntCfg().getRevisionInfoEntityName(), "r" );
        qb.getRootParameters().addWhere(
        		verCfg.getAuditEntCfg().getRevisionNumberPath(),
        		true,
        		"=",
        		"r.id",
        		false
        );

        List<Object> queryResult = buildAndExecuteQuery();
        if ( hasProjection ) {
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
                RevisionType revisionType = (RevisionType) versionsEntity.get( revisionTypePropertyName );
                if (revisionType == RevisionType.MOD) {
                    entities.add(new Object[] {entity, revisionData, revisionType, getChangedProperties(versionsEntity)});                	
                } else {
                    entities.add(new Object[] {entity, revisionData, revisionType});
                }
            }

            return entities;
        }
    }
	
	protected Number getRevisionNumber(Map versionsEntity) {
        AuditEntitiesConfiguration verEntCfg = verCfg.getAuditEntCfg();

        String originalId = verEntCfg.getOriginalIdPropName();
        String revisionPropertyName = verEntCfg.getRevisionFieldName();

        Object revisionInfoObject = ( (Map) versionsEntity.get(originalId)).get(revisionPropertyName );

        if ( revisionInfoObject instanceof HibernateProxy ) {
            return (Number) ((HibernateProxy) revisionInfoObject).getHibernateLazyInitializer().getIdentifier();
        } else {
            return verCfg.getRevisionInfoNumberReader().getRevisionNumber( revisionInfoObject );   
        }
    }
    
    protected Set<String> getChangedProperties(Map versionsEntity) {
    	Set<String> changedProperties = new HashSet<String>();
    	String modifiedFlagSuffix = verCfg.getGlobalCfg().getModifiedFlagSuffix();
    	for ( Object key : versionsEntity.keySet() ) {
    		String keyString = key.toString();
    		if ( keyString.endsWith( modifiedFlagSuffix ) ) {
    			Object value = versionsEntity.get(key);
    			if ( (value != null) && Boolean.parseBoolean( value.toString() ) ) {
    				changedProperties.add( keyString.substring( 0, keyString.length() - modifiedFlagSuffix.length() ) );
    			}
    		}
    	}
    	return changedProperties;
    }
}
