# DataOps

A simple program to run sql scripts against databases(currently MySQL).

## Build

```bash
mvn clean install
```

## Config

You need to have a config file at `~/.dataops.yaml`:

```
host: <db_host>
db: <db_name>
user: <user_name>
password: <password>
```

And you need to set the following environment variables:

```bash
export DATAOPS_HOME=<path-to-your-dataops-dir>
export PATH=$DATAOPS_HOME/bin:$PATH
```

## Usage

Current ability:

### Run sql with specified sqlFile and parallelism

```bash
dataops run <sqlPath> <parallelism>
```

Sample output:

```bash
➜ dataops run normal.sql 5
Host    : mysql.test.com
Db      : test_db
User    : test_user
Password: test_pass

[Thread4-0] Status: OK, RT: 1029ms
[Thread2-0] Status: OK, RT: 1030ms
[Thread4-1] Status: OK, RT: 716ms
[Thread2-1] Status: OK, RT: 975ms
[Thread4-2] Status: OK, RT: 812ms
[Thread2-2] Status: OK, RT: 901ms
[Thread3-2] Status: OK, RT: 850ms
```