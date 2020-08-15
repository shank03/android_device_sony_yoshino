/*
 * Copyright (C) 2007, The Android Open Source Project
 * Copyright (c) 2016, The CyanogenMod Project
 * Copyright (c) 2018-2020, The LineageOS Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *  * Neither the name of The Linux Foundation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/properties.h>
#include <sstream>
#include <fstream>
#include <iostream>
#include <chrono>
#include <ctime>

#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#define LOG_TAG "init_yoshino : "
#include <sys/_system_properties.h>

#include "vendor_init.h"
#include "property_service.h"

#include "ta.h"

#include "target_init.h"

using android::base::GetProperty;
using android::base::WaitForProperty;
using android::base::ReadFileToString;
using android::init::property_set;

using namespace std::chrono_literals;

static void load_properties_from_file(const char *, const char *);

static bool first = true;
static void log(const std::string &msg, bool warn = false) {
    if (warn) {
        PLOG(WARNING) << LOG_TAG << msg;
    } else {
        LOG(INFO) << LOG_TAG << msg;
    }

    // write log to file
    std::ofstream logFile;
    if (first) {
        // overwrite file with new logs
        logFile.open("/ocm/oem.log", std::ofstream::trunc);

        // getting date & time
        auto time = std::chrono::system_clock::now();
        std::time_t f_time = std::chrono::system_clock::to_time_t(time);

        logFile << "Created at " << std::ctime(&f_time) << std::endl;
        first = false;
    } else {
        logFile.open("/ocm/oem.log", std::ofstream::app);
    }
    logFile << msg << std::endl;
    logFile.close();
}

static bool isCH = false;
static bool isLocaleCH(const char *val) {
    std::string l[] = {"zh-CN", "zh-HK", "zh-TW"};
    for (auto & i : l) {
        if (strcmp(val, &i[0]) == 0) {
            return true;
        }
    }
    return false;
}

static void checkCH() {
    std::string files[] = {"/system/build.prop", "/system/default.prop", "/system/etc/prop.default",
        "/vendor/build.prop", "/vendor/default.prop", "/vendor/odm/etc/build.prop"};

    for (auto & i : files) {
        std::fstream file(i);
        log("Checking " + i);
        std::string r;
        while (getline(file, r)) {
            char *k = &r[0];    // get the whole line
            char *pos;          // initialize the position variable

            // make sure the line we got is not a '#' comment or blank line
            if (k[0] != '#' && k[0] != '\0') {
                char prop[1024];             // create prop key buffer
                pos = strchr(k, '=');        // get position of '=' char
                char *val = strtok(k, "=");  // get value of the prop
                if (val != nullptr) {
                    val = strtok(nullptr, "=");
                }

                // if position of '=' is not null and value is not null
                if (pos != nullptr && val != nullptr) {
                    strncpy_s(prop, k, pos - k);    // get the prop key
                    if (strcmp(prop, "ro.product.locale") == 0 && isLocaleCH(val)) {   // if locale prop is present ...
                        isCH = true;
                        throw std::exception("Locale error !");       // throw exception
                    } else {
                        if (!isCH) {
                            isCH = false;
                        }
                    }
                }
            }
        }
    }
}

static void load_properties(char *data, const char *filter)
{
    char *key, *value, *eol, *sol, *tmp, *fn;
    size_t flen = 0;
    if (filter) {
        flen = strlen(filter);
    }
    sol = data;
    while ((eol = strchr(sol, '\n'))) {
        key = sol;
        *eol++ = 0;
        sol = eol;
        while (isspace(*key)) key++;
        if (*key == '#') continue;
        tmp = eol - 2;
        while ((tmp > key) && isspace(*tmp)) *tmp-- = 0;
        if (!strncmp(key, "import ", 7) && flen == 0) {
            fn = key + 7;
            while (isspace(*fn)) fn++;
            key = strchr(fn, ' ');
            if (key) {
                *key++ = 0;
                while (isspace(*key)) key++;
            }
            load_properties_from_file(fn, key);
        } else {
            value = strchr(key, '=');
            if (!value) continue;
            *value++ = 0;
            tmp = value - 2;
            while ((tmp > key) && isspace(*tmp)) *tmp-- = 0;
            while (isspace(*value)) value++;
            if (flen > 0) {
                if (filter[flen - 1] == '*') {
                    if (strncmp(key, filter, flen - 1)) continue;
                } else {
                    if (strcmp(key, filter)) continue;
                }
            }
            if (strcmp(key, "ro.build.description") == 0
                || strcmp(key, "ro.build.version.incremental") == 0
                || strcmp(key, "ro.build.tags") == 0
                || strcmp(key, "ro.build.fingerprint") == 0
                || strcmp(key, "ro.vendor.build.fingerprint") == 0
                || strcmp(key, "ro.bootimage.build.fingerprint") == 0) {
                log("Skipped prop: " + std::string(key));
            } else {
                log("Load prop: " + std::string(key));
                property_set(key, value);
            }
        }
    }
}

static void load_properties_from_file(const char* filename, const char* filter) {
    std::string data;

    if (!ReadFileToString(filename, &data)) {
        log("Couldn't load property file", true);
        return;
    }
    data.push_back('\n');
    load_properties(&data[0], filter);
    log("Loaded properties from " + std::string(filename) + ".");
    return;
}

void vendor_load_properties() {

    // Wait for up to 2 seconds for /oem to be ready before we proceed (it should take much less...)
    WaitForProperty("ro.boot.oem.ready", "true", 2s);
    checkCH();

    if (!isCH) {
        log("Loading region- and carrier-specific properties from ocm");
        load_properties_from_file("/ocm/system-properties/cust.prop", NULL);

        // Get the active customization id from miscTA
        std::string cust_id = ta_get_cust_active();
        // If no customization is set, load the basic set of config props.
        if (cust_id.empty()) {
            log("No active customization detected.");
            log("Loading properties from /ocm/system-properties/config.prop");
            load_properties_from_file("/ocm/system-properties/config.prop", NULL);
        } else {
            // Otherwise, load the carrier-specific ones (these also contain the basic ones).
            log("Active customization detected: " + cust_id);
            std::stringstream ss;
            ss << "/ocm/system-properties/" << cust_id << "/config.prop";
            std::string cust_path = ss.str();
            log("Loading properties from " + cust_path);
            load_properties_from_file(cust_path.c_str(), NULL);
        }

        // Loading props from specific file -> oem.prop
        if (std::ifstream("/ocm/oem.prop")) {
            log("File oem.prop exists.\nLoading properties from oem.prop");
            load_properties_from_file("/ocm/oem.prop", NULL);
        } else {
            log("Please put oem.prop file in /oem directory");
        }
    }
}
