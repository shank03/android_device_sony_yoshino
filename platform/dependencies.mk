### CAMERA
PRODUCT_PACKAGES += \
    libmmcamera_interface \
    libmmjpeg_interface \
    libmmlib2d_interface \
    libmm-qcamera \
    libqomx_core

## IAshmemDeviceService
PRODUCT_PACKAGES += libashmemd_client

## Extra SecureElement interface
PRODUCT_PACKAGES += \
	android.hardware.secure_element@1.0 \
	android.hardware.secure_element@1.1

#GNSS HAL
PRODUCT_PACKAGES += \
    libgps.utils \
    libgnss \
    liblocation_api \
    android.hardware.gnss@1.0-impl-qti \
    android.hardware.gnss@1.0-service-qti

## Include MODEM (yoshino/hardware/modem)
include device/sony/yoshino/hardware/modem/Android.mk

PRODUCT_PACKAGES += \
	dump_miscta \
	ta_cust_version
