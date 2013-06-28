include_recipe "apt"
include_recipe "apache2"
include_recipe "apache2::mod_ssl"
include_recipe "apache2::mod_proxy"
include_recipe "apache2::mod_proxy_ajp"

# Generate self-signed cert.
# The snakeoil cert is already included with the ssl-cert package, 
# but we regenerate it in case the hostname has changed.
execute "make-ssl-cert" do
  command "make-ssl-cert generate-default-snakeoil --force-overwrite"
  ignore_failure true
  action :nothing
end

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


