Name:       smart-trip
Summary:    SmartM3 KPs for tourist route planning (Meta package)
Version:    0.1.2
Release:    1
Group:      Productivity/Networking/Other
License:    GPL-2.0
URL:        https://github.com/oss-fruct-org/smart-trip
Source0:    %{name}_%{version}.orig.tar.xz
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
%if 0%{?cenos_version} < 600
BuildRequires: curl-devel
%else
BuildRequires: libcurl-devel
%endif
BuildRequires: glib2-devel
%endif

BuildRequires: cmake
BuildRequires:  gcc-c++
BuildRequires: smartslog-codegen

%if 0%{?centos_version} > 0
BuildRequires: java-1.6.0-openjdk
%endif

AutoReqProv:    1
#Requires(post):

#########################
Requires: time-review-kp
Requires: time-plan-kp
Requires: geo-wm-kp
Requires: geo-db-kp
Requires: transport-kp
Requires: geo-names-kp
 
%description
 The project provides ability to find points to visit, create route and trip plan.

#########################
%package -n smart-trip-core
Group:      Productivity/Networking/Other
Summary: Template config file and libraries for Smart Trip project
Requires:       smart-trip = %{version} 

%description -n smart-trip-core
 Package includes template config file and libraries for KPs in Smart Trip project.
 It's fully work for local installation.

#########################
%package -n time-review-kp
Group:      Productivity/Networking/Other
Requires:       smart-trip-config = %{version} 
Summary: KP to calculate various times

%description -n time-review-kp
 KP loads movements from Smart Space and calculate wait time before movement's start.
 Also KP calculates visit time for points in Smart Space

#########################
%package -n time-plan-kp
Group:      Productivity/Networking/Other
Requires:       smart-trip-config = %{version} 
Summary: KP to calculate time plan

%description -n time-plan-kp
 KP loads route from Smart Space and calculate time plan

#########################
%package -n geo-wm-kp
Group:      Productivity/Networking/Other
Requires:       smart-trip-config = %{version} 
Summary: KP to load points from WikiMapia

%description -n geo-wm-kp
 KP loads points from WikiMapia and publish it to smart space

#########################
%package -n geo-db-kp
Group:      Productivity/Networking/Other
Requires:       smart-trip-config = %{version} 
Summary: KP to load points from DBPedia

%description -n geo-db-kp
 KP loads points from DBPedia and publish it to smart space

#########################
%package -n geo-names-kp
Group:      Productivity/Networking/Other
Requires:       smart-trip-config = %{version} 
Summary: KP to load points from Geonames service

%description -n geo-db-kp
 KP loads points from Geonames service and publish it to smart space

########################
%prep
%setup -q -n %{name}-%{version}
cd ontology && ./generate-ontology.sh && cd ..
 
%build
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%cmake -DSYSTEMD_CONFIGURATIONS_FILES_DIR=%{_unitdir}
%else
%cmake -DUPSTART_CONFIGURATIONS_FILES_DIR=/etc/init
%endif
%__make


%install
%if 0%{?mdkversion} || 0%{?centos_version} || 0%{?rhel_version}
make DESTDIR=$RPM_BUILD_ROOT install
%else
%if 0%{?fedora_version} == 0
cd build
%endif
%make_install
%endif

#### time-review-kp ###############
%pre -n time-review-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_pre time-review-kp.service
%endif

%preun -n time-review-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_preun time-review-kp.service
%endif

%post -n time-review-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_post time-review-kp.service
%endif

%postun -n time-review-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_postun time-review-kp.service
%endif

#### time-plan-kp #######
%pre -n time-plan-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_pre time-plan-kp.service
%endif

%preun -n time-plan-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_preun time-plan-kp.service
%endif

%post -n time-plan-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_post time-plan-kp.service
%endif

%postun -n time-plan-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_postun time-plan-kp.service
%endif

#### geo-wm-kp #######
%pre -n geo-wm-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_pre geo-wm-kp.service
%endif

%preun -n geo-wm-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_preun geo-wm-kp.service
%endif

%post -n geo-wm-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_post geo-wm-kp.service
%endif

%postun -n geo-wm-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_postun geo-wm-kp.service
%endif

#### geo-db-kp #######
%pre -n geo-db-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_pre geo-db-kp.service
%endif

%preun -n geo-db-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_preun geo-db-kp.service
%endif

%post -n geo-db-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_post geo-db-kp.service
%endif

%postun -n geo-db-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_postun geo-db-kp.service
%endif

#### geo-namaes-kp #######
%pre -n geo-names-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_pre geo-names-kp.service
%endif

%preun -n geo-names-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_preun geo-names-kp.service
%endif

%post -n geo-names-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_add_post geo-names-kp.service
%endif

%postun -n geo-names-kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%service_del_postun geo-names-kp.service
%endif

##### smart-trip-core ########
%post -n smart-trip-core
/sbin/ldconfig

%postun -n smart-trip-core
/sbin/ldconfig

##############################
%files -n smart-trip-core
%defattr(-,root,root,-)
%dir /etc/smart-trip
%config /etc/smart-trip/config.ini
%{_libdir}/lib*.so*

%files -n time-plan-kp
%defattr(-,root,root,-)
%{_bindir}/time_plan_kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%{_unitdir}/time-plan-kp.service
%endif

%files -n time-review-kp
%defattr(-,root,root,-)
%{_bindir}/time_review_kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%{_unitdir}/time-review-kp.service
%endif

%files -n geo-wm-kp
%defattr(-,root,root,-)
%{_bindir}/geo_wm_kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%{_unitdir}/geo-wm-kp.service
%endif

%files -n geo-db-kp
%defattr(-,root,root,-)
%{_bindir}/geo_db_kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%{_unitdir}/geo-db-kp.service
%endif

%files -n geo-names-kp
%defattr(-,root,root,-)
%{_bindir}/geo_names_kp
%if 0%{?centos_version} == 0 && 0%{?rhel_version} == 0
%{_unitdir}/geo-names-kp.service
%endif

%changelog
* Fri Apr 22 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.2
- Packaging Geonames kp
- improve kp integrations

* Mon Mar 28 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.1
- Packaging smart trip: WikiMapia and DBPedia

* Sun Mar 27 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.0
- Packaging smart trip: time plan and time review
