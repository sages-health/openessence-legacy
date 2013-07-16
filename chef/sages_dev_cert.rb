#!/usr/bin/env ruby

#
# Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
#                             All rights reserved.
#
# This material may be used, modified, or reproduced by or for the U.S.
# Government pursuant to the rights granted under the clauses at
# DFARS 252.227-7013/7014 or FAR 52.227-14.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
# WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
# LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
# INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
# RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
# LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,
# CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR
# INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
# FOR LOST PROFITS.
#

# Generate certs for SAGES development
class SagesDevCert
  require 'openssl'
  require 'securerandom'

  attr_reader :root_ca

  # The last serial number allocated.
  # Each cert signed by a CA must have a unique serial number.
  @@serial = -1

  def initialize
    @root_key = OpenSSL::PKey::RSA.new 2048
    @root_ca = generate_root_ca(@root_key)
  end

  def generate_root_ca(root_key)
    root_ca = OpenSSL::X509::Certificate.new
    root_ca.version = 2
    root_ca.serial = @@serial + 1
    root_ca.subject = OpenSSL::X509::Name.parse "/C=US/CN=SAGES Dev CA"
    root_ca.issuer = root_ca.subject # root CAs are self-signed
    root_ca.public_key = root_key.public_key
    root_ca.not_before = Time.now
    root_ca.not_after = root_ca.not_before + 2 * 365 * 24 * 60 * 60 # 2 years validity
    ef = OpenSSL::X509::ExtensionFactory.new
    ef.subject_certificate = root_ca
    ef.issuer_certificate = root_ca
    root_ca.add_extension(ef.create_extension("basicConstraints", "CA:TRUE", true))
    root_ca.add_extension(ef.create_extension("keyUsage", "keyCertSign, cRLSign", true))
    root_ca.add_extension(ef.create_extension("subjectKeyIdentifier", "hash", false))
    root_ca.add_extension(ef.create_extension("authorityKeyIdentifier", "keyid:always", false))
    root_ca.sign(root_key, OpenSSL::Digest::SHA256.new)

    root_ca
  end

  def generate_cert(common_name)
    key = OpenSSL::PKey::RSA.new(2048)
    subject = "/C=US/O=SAGES Health/CN=#{common_name}"

    cert = OpenSSL::X509::Certificate.new
    cert.subject = cert.issuer = OpenSSL::X509::Name.parse(subject)
    cert.issuer = @root_ca.subject
    cert.not_before = Time.now
    cert.not_after = Time.now + 365 * 24 * 60 *60
    cert.public_key = key.public_key
    cert.serial = @@serial + 1
    cert.version = 2

    ef = OpenSSL::X509::ExtensionFactory.new
    ef.subject_certificate = cert
    ef.issuer_certificate = @root_ca

    cert.add_extension(ef.create_extension("keyUsage", "digitalSignature", true))
    cert.add_extension(ef.create_extension("subjectKeyIdentifier", "hash", false))
    cert.sign(@root_key, OpenSSL::Digest::SHA256.new)

    { :cert => cert, :key => key }
  end
end

sages_dev_cert = SagesDevCert.new

['web', 'geoserver'].each { |node|
  # CA cert
  File.open("cookbooks/oe-#{node}/files/default/Sages_Dev_CA.crt", 'wb') { |f|
    # Debian convention is for CA certs to be PEM encoded with .crt extension
    f.print(sages_dev_cert.root_ca.to_pem)
  }

  cert_key = sages_dev_cert.generate_cert("#{node}.local")

  # signed cert
  File.open("cookbooks/oe-#{node}/files/default/#{node}.local.pem", 'wb') { |f|
    f.print(cert_key[:cert].to_pem)
  }

  # private key
  File.open("cookbooks/oe-#{node}/files/default/#{node}.local.key", 'wb') { |f|
    f.print(cert_key[:key].to_pem)
  }
}

