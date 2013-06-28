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

hostsfile_entry '127.0.0.1' do 
  hostname 'app.oe.local'
  aliases ['app.local']
  action :create
end

# TODO manage java version and update-alternatives

package 'tomcat7' do
  action :install
end

# install Tomcat APR package
package 'libtcnative-1' do
  action :install
end

# Allow Tomcat to read our private key
execute "usermod-tomcat" do
  command "usermod -a -G ssl-cert tomcat7"
end

template '/etc/tomcat7/server.xml' do
  source 'server.xml.erb'
  owner 'root'
  group 'tomcat7'
  mode '0644'
  notifies :restart, 'service[tomcat7]', :immediately
end

service 'tomcat7' do
  action [:enable, :start]
end



