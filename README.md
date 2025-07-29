# stock-analysis-service

## Database Setup
Execute the following SQL commands to create the schema, user, and grant the necessary privileges for the service:

Create a database schema for stock analysis service.
CREATE SCHEMA IF NOT EXISTS stock_analysis;

CREATE USER stockanalysisservice WITH PASSWORD 'ywY3a2to';

GRANT USAGE ON SCHEMA stock_analysis TO stockanalysisservice;
GRANT CREATE ON SCHEMA stock_analysis TO stockanalysisservice;

-- Allow user to select, insert, update, delete on all current tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA stock_analysis TO stockanalysisservice;

-- Ensure future tables will have these privileges as well
ALTER DEFAULT PRIVILEGES IN SCHEMA stock_analysis
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO stockanalysisservice;

to run any liquibase commmands, use below command:
pull liquibase docker image
docker run -it --rm -v /Users/darshilshah/IdeaProjects/stock-analysis-service/application/etc/database/:/liquibase/changelog liquibase /bin/sh
liquibase --changeLogFile=changelog.yml --url="jdbc:postgresql://host.docker.internal:5432/stock_analysis?currentSchema=stock_analysis" --username=stockanalysisservice --password=ywY3a2to status


for creating partitions:
brew install pg_partman


------------new --------------------
created a stock_analysis schema
grant usage and permissoon on the stock_analysis schema to stockanalysisservice user

GRANT CREATE ON DATABASE stock_analysis TO stockanalysisservice;


sudo ln -sf $HOME/.colima/default/docker.sock /var/run/docker.sock

## Database Setup
Execute the following SQL commands to create the schema, user, and grant the necessary privileges for the service:

Create a database schema for stock analysis service.
CREATE SCHEMA IF NOT EXISTS stock_analysis;

CREATE USER stockanalysisservice WITH PASSWORD 'ywY3a2to';

GRANT USAGE ON SCHEMA stock_analysis TO stockanalysisservice;
GRANT CREATE ON SCHEMA stock_analysis TO stockanalysisservice;

-- Allow user to select, insert, update, delete on all current tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA stock_analysis TO stockanalysisservice;

-- Ensure future tables will have these privileges as well
ALTER DEFAULT PRIVILEGES IN SCHEMA stock_analysis
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO stockanalysisservice;

to run any liquibase commmands, use below command:
pull liquibase docker image
docker run -it --rm -v /Users/darshilshah/IdeaProjects/stock-analysis-service/application/etc/database/:/liquibase/changelog liquibase /bin/sh
liquibase --changeLogFile=changelog.yml --url="jdbc:postgresql://host.docker.internal:5432/stock_analysis?currentSchema=stock_analysis" --username=stockanalysisservice --password=ywY3a2to status


for creating partitions:
brew install pg_partman


------------new --------------------
created a stock_analysis schema
grant usage and permissoon on the stock_analysis schema to stockanalysisservice user

GRANT CREATE ON DATABASE stock_analysis TO stockanalysisservice;


sudo ln -sf $HOME/.colima/default/docker.sock /var/run/docker.sock
