ExtAuditReader auditReader = ExtAuditReaderFactory.get(...);
ExtAuditQueryCreator auditQueryCreator = auditReader.createQuery();
AuditQuery auditQuery = auditQueryCreator
    .forRevisionsOfEntityAndChanges(<klasa>, true)
    .add(AuditEntity.id().eq(<identyfikator>));
List<Object[]> resultList = auditQuery.getResultList();
for (Object[] resultRow : resultList) {
    if (((RevisionType) resultRow[2]) == RevisionType.MOD) {
        System.out.println(
            "atrybuty zmienione w rewizji " +
            ((DefaultRevisionEntity) resultRow[1]).getId() +
            " to " + resultRow[3]
        );
    }
}