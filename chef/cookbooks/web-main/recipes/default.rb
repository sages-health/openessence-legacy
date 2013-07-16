include_recipe "apt"
include_recipe "apache2"
include_recipe "apache2::mod_ssl"
include_recipe "apache2::mod_proxy"
include_recipe "apache2::mod_proxy_ajp"

# Add DNS aliases
hostsfile_entry '127.0.0.1' do 
  hostname 'web.oe.local'
  aliases ['web.local']
  action :create
end
hostsfile_entry '192.168.22.3' do
  hostname 'app.oe.local'
  aliases ['app.local']
  action :create
end
hostsfile_entry '192.168.22.5' do
  hostname 'geoserver.oe.local'
  aliases ['geoserver.local']
  action :create
end

web_app 'frontend.oe.local' do
  template "frontend-oe-local.conf.erb"
end

# Begin CA cert install process
directory '/usr/share/ca-certificates/local' do
  owner 'root'
  group 'root'
  mode 0755
  action :create
end

cookbook_file '/usr/share/ca-certificates/local/Sages_Dev_CA.crt' do
  source 'Sages_Dev_CA.crt'
  owner 'root'
  group 'root'
  mode 0644
end

# There's lots of wrong advice on this.
# See http://blog.lib.umn.edu/ajz/infotech/2012/02/ubuntu-and-java-keystores.html
# for the right way to add cert
bash 'add-frontend-cert' do
  code 'echo "local/Sages_Dev_CA.crt" >> /etc/ca-certificates.conf'
  not_if do
    File.readlines('/etc/ca-certificates.conf').grep(/local\/Sages_Dev_CA.crt/).any?
  end
end

execute 'update-ca-certificates' do
  user 'root'
  action :run
end

# End CA cert install process

cookbook_file '/etc/ssl/certs/web.local.pem' do
  source 'web.local.pem'
  owner 'root'
  group 'root'
  mode 0777
end

cookbook_file '/etc/ssl/private/web.local.key' do
  source 'web.local.key'
  owner 'root'
  group 'root'
  mode 0710
end
