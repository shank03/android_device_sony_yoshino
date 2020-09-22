### GRAPHICS
PRODUCT_PACKAGES += \
    copybit.msm8998 \
    gralloc.msm8998 \
    hwcomposer.msm8998 \
    memtrack.msm8998 \
    libdisplayconfig \
    liboverlay

PRODUCT_PACKAGES += \
	android.hardware.graphics.mapper@3.0

## CAS
#PRODUCT_PACKAGES += \
	android.hardware.cas.native@1.0 \
	android.hardware.cas@1.1 \
	android.hardware.cas@1.1-service \
	android.hardware.cas@1.0 \
	android.hardware.cas@1.0-service

### AUDIO
PRODUCT_PACKAGES += \
    libvolumelistener \
    bthost_if

# IPACM
PRODUCT_PACKAGES += \
    ipacm \
    IPACM_cfg.xml \
    libipanat \
    liboffloadhal

# DISPLAY
PRODUCT_PACKAGES += \
    vendor.lineage.livedisplay@2.0-service-sdm

# SENSORS
PRODUCT_PACKAGES += \
    libsensorndkbridge

# THERMAL
PRODUCT_PACKAGES += \
    thermal.msm8998

# VR
PRODUCT_PACKAGES += \
    vr.msm8998

# USB TRUST HAL
PRODUCT_PACKAGES += \
    vendor.lineage.trust@1.0-service

# COVER
PRODUCT_PACKAGES += \
    FlipFlap

# NetworkSwitcher
PRODUCT_PACKAGES += \
    NetworkSwitcher

# ChargerImpl
PRODUCT_PACKAGES += \
    ChargerImpl
