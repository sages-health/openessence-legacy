# Introduction
There are a few files that you need to add to this directory
to get an instance of OpenEssence up and running. The files are
not checked in because they're developer specific and/or contain
security credentials.

All .properties files found in this directory are added as
PropertySources. The following are the ones that are needed
for certain portions of OpenEssence to function.

# db.properties
Database connection parameters. Although defining connection 
parameters in db.properties is usually preferable, it is not 
required if the DataSource is available in the Environment 
(e.g. a Tomcat-managed JNDI DataSource).

See `EnvironmentConfig.mainDataSource()` for more info.

## Example
```
db.driverClass=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/openessence
db.username=postgres
db.password=
```

# maps.properties
Map related settings. See `MapConfig` for more info.

## Fields

### wmsserver
URL client should use for WMS service requests.
Using a relative URL is recommended so that we don't
run into cross-origin issues.

Default is `/geoserver/wms`.

### postgres.cleanup
How often old data should be removed, in PostgreSQL
interval syntax.

Default is `1 minute`.