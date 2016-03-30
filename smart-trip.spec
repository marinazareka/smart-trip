Name:       smart-trip
Summary:    SmartM3 KPs for tourist route planning (Meta package)
Version:    0.1.1
Release:    2
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
 
%description
 The project provides ability to find points to visist, create route and trip plan.

#########################
%package -n smart-trip-core
Summary: Template config file and libraries for Smart Trip project
Requires:       smart-trip = %{version} 

%description -n smart-trip-core
 Package includes template config file and libraries for KPs in Smart Trip project.
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
cd ontology && ./generate-ontology.sh && cd ..
 
%build
%cmake -DSYSTEMD_CONFIGURATIONS_FILES_DIR=%{_unitdir}
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
 
%pre
%service_add_pre time-review-kp.service
%service_add_pre time-plan-kp.service
%service_add_pre geo-wm-kp.service
%service_add_pre geo-db-kp.service

%preun
%service_del_preun time-review-kp.service
%service_del_preun time-plan-kp.service
%service_del_preun geo-wm-kp.service
%service_del_preun geo-db-kp.service

%post 
/sbin/ldconfig
%service_add_post time-review-kp.service
%service_add_post time-plan-kp.service
%service_add_post geo-wm-kp.service
%service_add_post geo-db-kp.service
 
%postun 
/sbin/ldconfig
%service_del_postun time-review-kp.service
%service_del_postun time-plan-kp.service
%service_del_postun geo-wm-kp.service
%service_del_postun geo-db-kp.service
 
%files -n smart-trip-core
%defattr(-,root,root,-)
%dir /etc/smart-trip
%config /etc/smart-trip/config.ini
%{_libdir}/lib*.so.*

%files -n time-plan-kp
%defattr(-,root,root,-)
%{_bindir}/time_plan_kp
%{_unitdir}/time-plan-kp.service

%files -n time-review-kp
%defattr(-,root,root,-)
%{_bindir}/time_review_kp
%{_unitdir}/time-review-kp.service

%files -n geo-wm-kp
%defattr(-,root,root,-)
%{_bindir}/geo_wm_kp
%{_unitdir}/geo-wm-kp.service

%files -n geo-db-kp
%defattr(-,root,root,-)
%{_bindir}/geo_db_kp
%{_unitdir}/geo-db-kp.service

%changelog
* Mon Mar 28 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.1
- Packaging smart trip: WikiMapia and DBPedia

* Sun Mar 27 2016 Kirill Kulakov <kulakov@cs.karelia.ru> - 0.1.0
- Packaging smart trip: time plan and time review
