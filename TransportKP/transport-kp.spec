Name:       transport-kp
Summary:    KP to create route between points
Version:    0.1.1
Release:    1
Group:      Java
License:    GPL-2
URL:        https://github.com/oss-fruct-org/smart-trip
Source0:    %{name}_%{version}.orig.tar.xz
BuildRoot:  %{_tmppath}/%{name}-root

BuildArch:  noarch

Requires: java

AutoReqProv:    1
#Requires(post):
 
%description
 KP creates route between list of points in trip.
 KP uses GraphHopper library and OpenStreetMaps.
 
%prep
%setup -q -n %{name}-%{version}
 

%install

mkdir -p %{buildroot}/usr/bin
cp linux/transport_kp %{buildroot}/usr/bin
mkdir -p %{buildroot}/usr/share/transport-kp
cp build/libs/*.jar %{buildroot}/usr/share/transport-kp
mkdir -p %{buildroot}/%{_unitdir}
cp linux/transport-kp.service %{buildroot}/%{_unitdir}

%pre
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_pre transport-kp.service
%endif

%preun
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_preun transport-kp.service
%endif

%post
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_post transport-kp.service
%endif

%postun
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_postun transport-kp.service
%endif

 
%files
%defattr(-,root,root,-)
/usr/bin/transport_kp
%dir /usr/share/transport-kp
/usr/share/transport-kp/*.jar
%{buildroot}/%{_unitdir}/transport-kp.service


%changelog
* Tue Mar 31 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.1
- Packaging application
