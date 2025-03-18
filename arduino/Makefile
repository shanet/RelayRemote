# The basics
# -------------------------------------------------------------------------------------------------------------
TARGET := $(shell echo $(PROJECT) | tr [:upper:] [:lower:])
DEVICE := /dev/ttyACM0
BAUD := 115200
CONFIG_PATH := config.yml

# Dependencies & tooling
# -------------------------------------------------------------------------------------------------------------
DEPENDENCIES_DIR := deps
PLATFORM_DIR := $(DEPENDENCIES_DIR)/$(ARCHITECTURE)

CC := $(DEPENDENCIES_DIR)/gcc-$(ARCHITECTURE)/bin/$(if $(filter avr,$(ARCHITECTURE)),avr-gcc,arm-none-eabi-gcc)
CPPC := $(DEPENDENCIES_DIR)/gcc-$(ARCHITECTURE)/bin/$(if $(filter avr,$(ARCHITECTURE)),avr-g++,arm-none-eabi-g++)
AR := $(DEPENDENCIES_DIR)/gcc-$(ARCHITECTURE)/bin/$(if $(filter avr,$(ARCHITECTURE)),avr-ar,arm-none-eabi-ar)
OBJCOPY := $(DEPENDENCIES_DIR)/gcc-$(ARCHITECTURE)/bin/$(if $(filter avr,$(ARCHITECTURE)),avr-objcopy,arm-none-eabi-objcopy)
UPLOADER := $(DEPENDENCIES_DIR)/$(if $(filter avr,$(ARCHITECTURE)),avrdude/avrdude,bossac/bossac)

# Board info
# -------------------------------------------------------------------------------------------------------------
ifdef BOARD
	GET_BOARDS_PARAM = $(shell sed -ne "s/$(BOARD).$(1)=\(.*\)/\1/p" $(PLATFORM_DIR)/boards.txt)
endif

BOARD_BOARD := $(call GET_BOARDS_PARAM,build.board)
BOARD_BOOTLOADER_FILE := $(call GET_BOARDS_PARAM,bootloader.file)
BOARD_BUILD_FCPU := $(call GET_BOARDS_PARAM,build.f_cpu)
BOARD_BUILD_MCU := $(call GET_BOARDS_PARAM,build.mcu)
BOARD_BUILD_VARIANT := $(call GET_BOARDS_PARAM,build.variant)
BOARD_LD_SCRIPT := $(call GET_BOARDS_PARAM,build.ldscript)
BOARD_USB_MANUFACTURER := $(call GET_BOARDS_PARAM,build.usb_manufacturer)
BOARD_USB_PID := $(call GET_BOARDS_PARAM,build.pid)
BOARD_USB_PRODUCT := $(call GET_BOARDS_PARAM,build.usb_product)
BOARD_USB_VID := $(call GET_BOARDS_PARAM,build.vid)

# Sources
# -------------------------------------------------------------------------------------------------------------
SOURCES := $(wildcard $(addprefix src/, *.c *.cpp))
HEADERS :=  $(wildcard $(addprefix src/, *.h))
OUTPUT := bin/$(TARGET)

# The variant file for the board contains information specific to the device we're compiling for
ifneq ($(BOARD_BUILD_VARIANT), standard)
	SOURCES += $(PLATFORM_DIR)/variants/$(BOARD_BUILD_VARIANT)/variant.cpp
endif

OBJECTS := $(addsuffix .o, $(basename $(SOURCES)))
COMPILED_OBJECTS := $(addprefix $(OUTPUT)/, $(addsuffix .o, $(basename $(SOURCES))))

# Flags
# -------------------------------------------------------------------------------------------------------------
ifeq ($(ARCHITECTURE), arm)
	ARCHITECTURE_FLAGS := -mcpu=$(BOARD_BUILD_MCU) -mthumb --specs=nano.specs --specs=nosys.specs
else ifeq ($(ARCHITECTURE), avr)
	ARCHITECTURE_FLAGS := -mmcu=$(BOARD_BUILD_MCU)
endif

CFLAGS := -c -g -Os -w -std=gnu11 -ffunction-sections -fdata-sections -nostdlib --param max-inline-insns-single=500 -MMD $(ARCHITECTURE_FLAGS)
CPPFLAGS := -c -g -Os -w -std=gnu++11 -ffunction-sections -fdata-sections -fno-threadsafe-statics -fno-rtti -fno-exceptions \
	-nostdlib --param max-inline-insns-single=500 -MMD $(ARCHITECTURE_FLAGS)
ASMFLAGS := -c -g -x assembler-with-cpp
ELFFLAGS := -Wall -Wextra -Os -g -flto -fuse-linker-plugin -Wl,--gc-sections -save-temps $(ARCHITECTURE_FLAGS)

ifneq ($(BOARD_LD_SCRIPT),)
	LD_SCRIPT := -T$(PLATFORM_DIR)/variants/$(BOARD_BUILD_VARIANT)/$(BOARD_LD_SCRIPT)
else
	LD_SCRIPT :=
endif

# Defines
# -------------------------------------------------------------------------------------------------------------
ifeq ($(ARCHITECTURE), arm)
	ARCHITECTURE_DEFINES := -DARDUINO_ARCH_SAMD -D__SAMD21G18A__ -DUSBCON -DUSB_VID=$(BOARD_USB_VID) \
		-DUSB_PID=$(BOARD_USB_PID) -DUSB_MANUFACTURER=\"$(BOARD_USB_MANUFACTURER)\" -DUSB_PRODUCT=\"$(BOARD_USB_PRODUCT)\" -DARDUINO=10605
else ifeq ($(ARCHITECTURE), avr)
	ARCHITECTURE_DEFINES := -DARDUINO_ARCH_AVR
endif

ifdef RELAY
	CONFIG := $(shell scripts/parse_config.py $(CONFIG_PATH) $(RELAY))
endif

DEFINES := -DF_CPU=$(BOARD_BUILD_FCPU) -DARDUINO_$(BOARD_BOARD) $(ARCHITECTURE_DEFINES) $(CONFIG)

# Includes
# -------------------------------------------------------------------------------------------------------------
INCLUDE_DIRS := lib/ \
	$(wildcard lib/*) \
	$(PLATFORM_DIR)/cores/arduino \
	$(PLATFORM_DIR)/variants/$(BOARD_BUILD_VARIANT) \
	$(PLATFORM_DIR)/libraries/SPI \
	$(PLATFORM_DIR)/libraries/SPI/src \
	$(PLATFORM_DIR)/libraries/Wire \
	$(PLATFORM_DIR)/libraries/Wire/src

ifeq ($(ARCHITECTURE), arm)
	INCLUDE_DIRS += $(DEPENDENCIES_DIR)/cmsis/CMSIS/Include/
	INCLUDE_DIRS += $(DEPENDENCIES_DIR)/cmsis/Device/ATMEL/
endif

# Add the include flag before each include
INCLUDES := $(foreach dir, $(INCLUDE_DIRS), \
	$(addprefix -I, $(dir)))

# Project libraries
# -------------------------------------------------------------------------------------------------------------
LIBRARY_SEARCH_PATHS ?= lib $(PLATFORM_DIR)/libraries

# Search the headers for what libraries are included
FOUND_LIBRARIES := $(filter $(notdir $(wildcard $(addsuffix /*, $(LIBRARY_SEARCH_PATHS)))), \
	$(shell sed -ne "s/^ *\# *include *[<\"]\(.*\)\.h[>\"]/\1/p" $(HEADERS)))

# Add the path to the found libraries
LIBRARY_DIRS := $(foreach lib, $(FOUND_LIBRARIES), \
	$(firstword $(wildcard $(addsuffix /$(lib), $(LIBRARY_SEARCH_PATHS)))))
LIBRARY_DIRS += $(foreach lib, $(FOUND_LIBRARIES), \
	$(firstword $(wildcard $(addsuffix /$(lib)/src, $(LIBRARY_SEARCH_PATHS)))))

# Explicitly add everything in lib/
EXPLICIT_LIBRARIES = $(notdir $(wildcard lib/*))

LIBRARY_DIRS += $(foreach lib, $(EXPLICIT_LIBRARIES), \
	$(shell find lib/$(lib) -type d))

# Platform libraries
# -------------------------------------------------------------------------------------------------------------
ARDUINO_CORE_DIR := $(PLATFORM_DIR)/cores/arduino/ \
	$(PLATFORM_DIR)/cores/arduino/USB/ \
	$(PLATFORM_DIR)/cores/arduino/avr/

# Link all the platform libraries into a single archive
CORE_LIB := $(OUTPUT)/core.a
CORE_LIB_OBJECTS := $(foreach dir, $(ARDUINO_CORE_DIR) $(LIBRARY_DIRS), \
	$(patsubst %, $(OUTPUT)/%.o, $(wildcard $(addprefix $(dir)/, *.c *.cpp *.S))))

# Recipes
# -------------------------------------------------------------------------------------------------------------
.PHONY:	all wired_server_build wifi_server_build wifi_client_build build_arm build_avr wired_server wifi_server wifi_client upload_avr upload_arm console clean

all: wired_server_build wifi_server_build wifi_client_build

wired_server_build:
	$(MAKE) ARCHITECTURE=avr BOARD=uno PROJECT=WIRED_SERVER build_avr

wifi_server_build:
	$(MAKE) ARCHITECTURE=arm BOARD=adafruit_feather_m0 PROJECT=WIFI_SERVER build_arm

wifi_client_build:
	$(MAKE) ARCHITECTURE=arm BOARD=adafruit_feather_m0 PROJECT=WIFI_CLIENT build_arm

build_arm: $(TARGET).bin
build_avr: $(TARGET).hex

wired_server: wired_server_build
	$(MAKE) ARCHITECTURE=avr PROJECT=WIRED_SERVER upload_avr

wifi_server: wifi_server_build
	$(MAKE) ARCHITECTURE=arm PROJECT=WIFI_SERVER upload_arm

wifi_client: wifi_client_build
	$(MAKE) ARCHITECTURE=arm PROJECT=WIFI_CLIENT upload_arm

upload_avr:
	$(UPLOADER) -C $(DEPENDENCIES_DIR)/avrdude/etc/avrdude.conf -v -p atmega328p -c arduino -P $(DEVICE) -D -U flash:w:$(OUTPUT)/$(TARGET).hex:i

upload_arm:
	$(UPLOADER) --port=$(notdir $(DEVICE)) --force_usb_port=true --info --erase --write --verify --reset $(OUTPUT)/$(TARGET).bin

console:
	screen $(DEVICE) $(BAUD)

$(TARGET).bin: $(TARGET).elf
	$(OBJCOPY) -O binary $(OUTPUT)/$< $(OUTPUT)/$@

$(TARGET).hex: $(TARGET).elf
	$(OBJCOPY) -O ihex -j .eeprom --set-section-flags=.eeprom=alloc,load --no-change-warnings --change-section-lma .eeprom=0 $(OUTPUT)/$< $(OUTPUT)/$@
	$(OBJCOPY) -O ihex -R .eeprom $(OUTPUT)/$< $(OUTPUT)/$@

$(TARGET).elf: $(CORE_LIB) $(OBJECTS)
	$(CC) -L$(OUTPUT) ${LD_SCRIPT} \
		-Wl,-Map,$(OUTPUT)/$(TARGET).map $(ELFFLAGS) -o $(OUTPUT)/$@ $(COMPILED_OBJECTS) \
		-lm $(CORE_LIB)

# Build source files
# -------------------------------------------------------------------------------------------------------------
%.o: %.c
	mkdir --parents $(OUTPUT)$(dir $<)
	$(CC) $(CFLAGS) -D$(PROJECT) $(DEFINES) $(INCLUDES) -o $(OUTPUT)/$@ $<

%.o: %.cpp
	mkdir --parents $(OUTPUT)/$(dir $<)
	$(CPPC) $(CPPFLAGS) -D$(PROJECT) $(DEFINES) $(INCLUDES) -o $(OUTPUT)/$@ $<

# Build library files
# -------------------------------------------------------------------------------------------------------------
$(CORE_LIB): $(CORE_LIB_OBJECTS)
	$(AR) rcs $@ $?

$(OUTPUT)/%.c.o: %.c
	mkdir --parents $(dir $@)
	$(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -o $@ $<

$(OUTPUT)/%.cpp.o: %.cpp
	mkdir --parents $(dir $@)
	$(CPPC) $(CPPFLAGS) $(DEFINES) $(INCLUDES) -o $@ $<

$(OUTPUT)/%.S.o: %.S
	mkdir --parents $(dir $@)
	$(CC) $(CPPFLAGS) $(DEFINES) $(INCLUDES) -o $@ $<

clean:
	rm -rf bin
	mkdir bin
