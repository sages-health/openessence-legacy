# Provisioning OpenEssence
These cookbooks are used by Vagrant to set up an OpenEssence dev environment.
Unfortunately, they're not yet able to provision full production systems with
no user intervention, but pull requests are welcome.

OpenEssence has two sets of cookbooks. Third-party cookbooks are managed by 
Librarian and live under **lib**. Cookbooks we maintain live under 
**cookbooks**.

## Librarian
Librarian is a package manager for chef cookbooks. We use Librarian through the
[vagrant-librarian-chef plugin](https://github.com/jimmycuadra/vagrant-librarian-chef),
so you shouldn't have to worry about it if you're running Vagrant.
