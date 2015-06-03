#ifndef CFUNCTION_HPP
#define CFUNCTION_HPP

#include <type_traits>
#include <functional>

#include <QMap>
#include <QDebug>
#include <QMutex>


template<typename T>
class CFunction {
    static QMap<T, std::function<void(T)>> m_wrappers;
    static QMutex m_mutex;

public:

public:
    template<typename F>
    static void wrap(T key, F cppFunction) {
        QMutexLocker lock(&m_mutex);

        auto iter = m_wrappers.find(key);
        if (iter == m_wrappers.end()) {
            m_wrappers.insert(key, cppFunction);
        }
    }

    static void unwrap(T key) {
        QMutexLocker lock(&m_mutex);
        m_wrappers.remove(key);
    }

    static void handler(T key) {
        QMutexLocker lock(&m_mutex);
        auto iter = m_wrappers.find(key);
        if (iter != m_wrappers.end()) {
            (*iter)(key);
        }
    }
};

template <typename T> QMap<T, std::function<void(T)>> CFunction<T>::m_wrappers;
template <typename T> QMutex CFunction<T>::m_mutex;

#endif // CFUNCTION_HPP

