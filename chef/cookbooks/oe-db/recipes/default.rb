include_recipe 'apt'

# postgresql-9.1-postgis in Precise is v1.5, so use PPA for v2.0
apt_repository 'ubuntugis' do
  uri 'http://ppa.launchpad.net/ubuntugis/ppa/ubuntu'
  distribution 'precise'
  components ['main']
  keyserver 'keyserver.ubuntu.com'
  key '314DF160' # See https://launchpad.net/~ubuntugis/+archive/ppa/
end

# install PostGIS, other postgres packages are auto-installed as deps
package 'postgresql-9.1-postgis' do
  action :install
end

# For stuff like adminpack and hstore
package 'postgresql-contrib-9.1' do
  action :install
end

# Use UTF-8 instead of latin1.
# The Internet claims you don't need to do this if you just set the locale correctly
# before you install postgres, but that's never worked for me.
execute 'drop-cluster' do
  user 'postgres'
  command 'pg_dropcluster --stop 9.1 main'
  only_if do
    File.exists?('/etc/postgresql/9.1/main')
  end
  not_if "psql -c 'SHOW SERVER_ENCODING' | grep 'UTF8'", :user => 'postgres'
end
execute 'create-cluster' do
  user 'root'
  command 'pg_createcluster --start --locale=C -e UTF-8 9.1 main'
  only_if do
    not File.exists?('/etc/postgresql/9.1/main')
  end
end

template '/etc/postgresql/9.1/main/postgresql.conf' do
  source 'postgresql.conf.erb'
  owner 'postgres'
  group 'postgres'
  mode '0644'
  notifies :restart, 'service[postgresql]', :immediately
end

template "/etc/postgresql/9.1/main/pg_hba.conf" do
  source "pg_hba.conf.erb"
  owner "postgres"
  group "postgres"
  mode '0640'
  notifies :reload, 'service[postgresql]', :immediately
end

service 'postgresql' do
  action [:enable, :start]
end

bash 'install-adminpack' do
  user 'postgres'
  code "echo 'CREATE EXTENSION adminpack' | psql"
end

bash "set-postgres-password" do
  user 'postgres'
  code <<-EOH
echo "ALTER ROLE postgres ENCRYPTED PASSWORD '#{node['oe-db']['password']}';" | psql
  EOH
  action :run

  # yes, you really do need to restart postgres
  notifies :restart, 'service[postgresql]', :immediately
end


