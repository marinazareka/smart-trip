Name:       smart-trip
Summary:    SmartM3 KPs for tourist route planning (Meta package)
Version:    0.1.0
Release:    1
Group:      Productivity/Networking/Other
License:    GPL-2.0
URL:        https://github.com/oss-fruct-org/smart-trip
Source0:    %{name}_0.1.orig.tar.xz
BuildRoot:  %{_tmppath}/%{name}-root

%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
BuildRequires: systemd
%endif

%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
BuildRequires: pkg-config
BuildRequires: pkgconfig(glib-2.0)
BuildRequires: pkgconfig(smartslog)
BuildRequires: pkgconfig(libcurl)
%endif

%if 0%{?centos_version} > 0 || 0%{?rhel_version} > 0
BuildRequires: pkgconfig
BuildRequires: libsmartslog0-devel
BuildRequires: libcurl-devel
BuildRequires: glib2-devel
%endif

AutoReqProv:    1
#Requires(post):

#########################
Requires: time-review-kp
Requires: time-plan-kp
Requires: geo-wm-kp
Requires: geo-db-kp
 
%description
 The project provides ability to find points to visist, create route and trip plan.

#########################
%package -n smart-trip-config
Summary: Template config file for Smart Trip project
Requires:       smart-trip = %{version} 

%description -n smart-trip-config
 Package includes template config file for KPs in Smart Trip project.
 It's fully work for local installation.

#########################
%package -n time-review-kp
Requires:       smart-trip-config = %{version} 
Summary: KP to calculate various times

%description -n time-review-kp
 KP loads movements from Smart Space and calculate wait time before movement's start.
 Also KP calculates visit time for points in Smart Space

#########################
%package -n time-plan-kp
Requires:       smart-trip-config = %{version} 
Summary: KP to calculate time plan

%description -n time-plan-kp
 KP loads route from Smart Space and calculate time plan

#########################
%package -n geo-wm-kp
Requires:       smart-trip-config = %{version} 
Summary: KP to load points from WikiMapia

%description -n geo-wm-kp
 KP loads points from WikiMapia and publish it to smart space

#########################
%package -n geo-db-kp
Requires:       smart-trip-config = %{version} 
Summary: KP to load points from DBPedia

%description -n geo-db-kp
 KP loads points from DBPedia and publish it to smart space

########################
%prep
%setup -q -n %{name}-%{version}
cd ontology && ./generate-ontology.sh >/dev/null 2>&1 && cd ..
 
%build
cmake
%__make


%install
rm -rf %{buildroot}
  
make install INSTALL_ROOT=$RPM_BUILD_ROOT 
 
%pre
%service_add_pre time-review-kp.service
%service_add_pre time-plan-kp.service

%preun
%service_del_preun time-review-kp.service
%service_del_preun time-plan-kp.service

%post 
/sbin/ldconfig
%service_add_post time-review-kp.service
%service_add_post time-plan-kp.service
 
%postun 
/sbin/ldconfig
%service_del_postun time-review-kp.service
%service_del_postun time-plan-kp.service
 
%files -n smart-trip-config
%defattr(-,root,root,-)
%config /etc/smart-trip/config.ini

%files -n time-plan-kp
%{_bindir}/time_plan_kp
%{_unitdir}/time-plan-kp.service

%files -n time-review-kp
%{_bindir}/time_review_kp
%{_unitdir}/time-review-kp.service

%changelog
* Fri Feb 12 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.0
- Packaging smart trip: time plan and time review
