include_recipe 'apt'

package 'ssl-cert' do
  action :install
end

# Generate self-signed cert.
# The snakeoil cert is already included with the ssl-cert package, 
# but we regenerate it in case the hostname has changed.
execute "make-ssl-cert" do
  command "make-ssl-cert generate-default-snakeoil --force-overwrite"
end

# install Tomcat APR package
package 'libtcnative-1' do
  action :install
end

apt_repository "opengeo" do
  uri "http://apt.opengeo.org/suite/v3/ubuntu"
  distribution "lucid" # They only have Lucid, but it still works on Precise
  components ["main"]
  key "http://apt.opengeo.org/gpg.key"
end

package "opengeo-geoserver" do
  action :install
  response_file 'geoserver.seed.erb'
end

# Delete password files so GeoServer stops complaining about security risks
file '/usr/share/opengeo-suite-data/geoserver_data/security/masterpw.info' do
  action :delete
end
file '/usr/share/opengeo-suite-data/geoserver_data/security/users.properties.old' do
  action :delete
end

# overwrite geoserver's built-in Tomcat settings
template "/etc/default/tomcat6" do
  source 'tomcat6.erb'
  owner 'root'
  group 'root'
  mode '0644'
  notifies :restart, 'service[tomcat6]', :delayed
end

template '/etc/tomcat6/server.xml' do
  source 'server.xml.erb'
  owner 'root'
  group 'tomcat6'
  mode '0644'
  notifies :restart, 'service[tomcat6]', :immediately
end

service 'tomcat6' do
  action [:enable, :start]
end

hostsfile_entry '192.168.22.4' do
  hostname 'db.oe.local'
  aliases ['db.local']
  action :create
end

# GeoServer often gets its own URL from client, so it needs to know about frontend proxy
hostsfile_entry '192.168.22.2' do
  hostname 'web.oe.local'
  aliases ['web.local']
  action :create
end

directory '/usr/share/ca-certificates/local' do
  owner 'root'
  group 'root'
  mode 0755
  action :create
end

# There's lots of wrong advice on this.
# See http://blog.lib.umn.edu/ajz/infotech/2012/02/ubuntu-and-java-keystores.html
# for the right way to add cert
bash 'add-frontend-cert' do
  code 'echo "local/web.local.crt" >> /etc/ca-certificates.conf'
  not_if do
    File.readlines('/etc/ca-certificates.conf').grep(/local\/web.local.crt/).any?
  end
end
execute 'update-ca-certificates' do
  user 'root'
  action :run
end

