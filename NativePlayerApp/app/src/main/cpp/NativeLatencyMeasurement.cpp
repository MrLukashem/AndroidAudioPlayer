//
// Created by MrLukashem on 22.04.2017.
//

#include "include/NativeLatencyMeasurement.h"

#include <sstream>
#include <android/log.h>
#include <math.h>
#include <thread>
#include <condition_variable>
#include <assert.h>
#include <pthread.h>
#include <tuple>

#define APPNAME "android_native_player_jni"
#define ALOGV(x) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, x);

namespace {
    std::condition_variable cv;
    std::mutex cv_m;

    auto to_string = [](auto value) -> std::string {
        std::ostringstream ss;
        ss << value;

        return ss.str();
    };

    auto amplitude_to_db = [](int amplitude) -> double {
    //    double pa = amplitude * 0.000038614 + 0.0002;
        return 20 * log10(amplitude / 32767.0);
    };

    NativeLatencyMeasurement* self = nullptr;
    pthread_mutex_t mutex;

    void inputBufferCallback(SLAndroidSimpleBufferQueueItf itf, void *context) {
        ++self->m_totalCallbacks;
   //     __android_log_print(ANDROID_LOG_INFO, "cb", "%d", self->m_totalCallbacks);

    //   pthread_mutex_lock(&mutex);

        assert(self != nullptr);
        self->processMeasurement(itf, context);

        //
        //;
    }
}

constexpr SLuint32 MAX_NUMBER_INPUTS_DEVICES = 3;

NativeLatencyMeasurement::NativeLatencyMeasurement(std::function<void(int)> jniCallback, std::function<void(void)> playRequestCallback)
: m_jniCallback {jniCallback}, m_playRequestCallback {playRequestCallback} {

    m_bitDepth = UN_INITIALIZED;
    m_sampleRate = UN_INITIALIZED;
    m_environmentNoise = UN_INITIALIZED;

	auto error = initialize();
	if (error != SL_RESULT_SUCCESS) { // Throw exception because we can not initialize OpenSL engine.
		throw std::runtime_error("OpenSLES init error = " + to_string(error));
	}

    self = this;
}

void NativeLatencyMeasurement::deinitOpenSL() {
    (*m_recorderBufferQueue)->Destroy(m_recorderBufferQueue);
    (*m_objectItf)->Destroy(m_objectItf);
}

NativeLatencyMeasurement::~NativeLatencyMeasurement() {
    ALOGV("!DESTROY");
	deinitOpenSL();

    if (m_inputBuffer != nullptr) {
        delete m_inputBuffer;
    }
}

int NativeLatencyMeasurement::initialize() {
	SLEngineOption engineOptions[] = {
		{ 
			(SLuint32) SL_ENGINEOPTION_THREADSAFE,
			(SLuint32) SL_BOOLEAN_TRUE 
		},
	};

	auto res = slCreateEngine(&m_objectItf, 1, engineOptions, 0, NULL, NULL);
    if (res != SL_RESULT_SUCCESS) {
        return res;
    }

	res = (*m_objectItf)->Realize(m_objectItf, SL_BOOLEAN_FALSE);
	if (res != SL_RESULT_SUCCESS) {
		return res;
	}

	// Initialize engine.
	res = (*m_objectItf)->GetInterface(m_objectItf, SL_IID_ENGINE, static_cast<void*>(&m_engineItf));
	if (res != SL_RESULT_SUCCESS) {
		return res;
	}

	return res;
}

SLuint32 NativeLatencyMeasurement::initializeRecorder() {
	SLDataSource audioSource;
	SLDataSink dataSink;

	// Init source; Example microphone.
	SLDataLocator_IODevice deviceInputLocator = {
		SL_DATALOCATOR_IODEVICE,
		SL_IODEVICE_AUDIOINPUT,
		SL_DEFAULTDEVICEID_AUDIOINPUT,
		nullptr
	};
	audioSource = {
		&deviceInputLocator,
		nullptr,
	};

    m_sampleRate = 44100;
    m_bitDepth = 16;
	// Init sink; Recording input.
    SLDataLocator_AndroidSimpleBufferQueue inputLocator = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            1 // 1 Buffer!
    };
    SLuint16 bitsPerSample = static_cast<SLuint16>(m_bitDepth);
    SLuint16 containerSize = 0;
    if (bitsPerSample > SL_PCMSAMPLEFORMAT_FIXED_16) {
        containerSize = SL_PCMSAMPLEFORMAT_FIXED_32;
    } else {
        containerSize = bitsPerSample;
    }
    SLDataFormat_PCM inputFormat = {
            SL_DATAFORMAT_PCM, // formatType.
            1, // num of channels.
            static_cast<SLuint32 >(m_sampleRate * 1000), // sample rate.
            static_cast<SLuint32 >(m_bitDepth), // bits per sample.
            containerSize,
            SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN,
    };
    dataSink = {
            &inputLocator,
            &inputFormat,
    };

    const SLInterfaceID inputInterfaces[2] = {
            SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
            SL_IID_ANDROIDCONFIGURATION
    };
    SLboolean requireds[2] = { SL_BOOLEAN_TRUE, SL_BOOLEAN_FALSE };
	auto res = (*m_engineItf)->CreateAudioRecorder(m_engineItf, &m_recorderBufferQueue,
        &audioSource, &dataSink, 2, inputInterfaces, requireds);
    if (res != SL_RESULT_SUCCESS) {
        return res;
    }

    SLAndroidConfigurationItf  inputConfiguration;
    res = (*m_recorderBufferQueue)->GetInterface(m_recorderBufferQueue, SL_IID_ANDROIDCONFIGURATION,
        &inputConfiguration);
    if (res == SL_RESULT_SUCCESS) {
        SLuint32 preset = SL_ANDROID_RECORDING_PRESET_VOICE_RECOGNITION;
        res = (*inputConfiguration)->SetConfiguration(inputConfiguration, SL_ANDROID_KEY_RECORDING_PRESET,
            &preset, sizeof(SLuint32));
        if (res != SL_RESULT_SUCCESS) {
            return res;
        }
    }

    return (*m_recorderBufferQueue)->Realize(m_recorderBufferQueue, SL_BOOLEAN_FALSE);
}

SLuint32 NativeLatencyMeasurement::startRecording() {
    SLAndroidSimpleBufferQueueItf inputBufferQueueItf;
    SLRecordItf recordItf;

    auto res = (*m_recorderBufferQueue)->GetInterface(m_recorderBufferQueue,
                                                      SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                                      &inputBufferQueueItf);
    if (res != SL_RESULT_SUCCESS) {
        return res;
    }

    res = (*inputBufferQueueItf)->RegisterCallback(inputBufferQueueItf, inputBufferCallback, nullptr);
    if (res != SL_RESULT_SUCCESS) {
        return res;
    }

    res = (*m_recorderBufferQueue)->GetInterface(m_recorderBufferQueue, SL_IID_RECORD, &recordItf);
    if (res != SL_RESULT_SUCCESS) {
        return res;
    }

    m_inputBuffer = new short[128]; // TODO: Have to chnage value acording to AudioSystem getBufferSize.
    m_inputBufferSize = 128;

    res = (*inputBufferQueueItf)->Enqueue(inputBufferQueueItf, static_cast<void*>(m_inputBuffer), m_inputBufferSize*2);
    if (res != SL_RESULT_SUCCESS) {
        return res;
    }

    res =  (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);

    std::thread playRequestThread([this]() {
        std::unique_lock<std::mutex> lk(cv_m);
        cv.wait(lk);

       triggerJNIPlayRequestCallback();
    });
    playRequestThread.detach();

    return res;
}

int NativeLatencyMeasurement::startTest(int bitDepth, int sampleRate) {
    m_bitDepth = bitDepth;
    m_sampleRate = sampleRate;

    SLuint32 res;
    if ((res = initializeRecorder()) != SL_RESULT_SUCCESS) {
		return res;
	}

	res = startRecording();
    return static_cast<int>(res);
}

int NativeLatencyMeasurement::getMicDeviceID() {
/*    SLAudioIODeviceCapabilitiesItf ioDeviceCapabilitiesItf;
    SLint32 numInputs = 0;
    SLuint32 devicesIDs[MAX_NUMBER_INPUTS_DEVICES];

    auto res = (*m_objectItf)->GetInterface(
    	m_objectItf, SL_IID_AUDIOIODEVICECAPABILITIES, static_cast<void*>(&ioDeviceCapabilitiesItf));
    if (res != SL_RESULT_SUCCESS) {
    	return res;
    }

    res = (*ioDeviceCapabilitiesItf)->GetAvailableAudioInputs(ioDeviceCapabilitiesItf, &numInputs, devicesIDs);
   	if (res != SL_RESULT_SUCCESS) {
   		return res;
   	}

   	SLAudioInputDescriptor audioIODesc;
    for (int i = 0; i < numInputs; i++) {
        res = (*ioDeviceCapabilitiesItf)->(ioDeviceCapabilitiesItf, devicesIDs[i], &audioIODesc);
        if (res != SL_RESULT_SUCCESS) {
        	return res;
        }

        if (audioIODesc.deviceLocation == SL_DEVLOC)
        
    }
*/
	return 0;
}

std::pair<int, int> NativeLatencyMeasurement::getAmplitudeSum(int& amplitudeSamples) { // Not thread safe.
    int amplitudeSum = 0;
    int totalSamples = m_inputBufferSize;

    for (int i = 0; i < m_inputBufferSize; i++) {
       // if (m_inputBuffer[i] > 0) {
            amplitudeSum += static_cast<int>(fabs(m_inputBuffer[i]));
        //} else {
        //   totalSamples--;
      //  }
    }

    amplitudeSamples = m_inputBufferSize;
    return std::make_pair(amplitudeSum, m_inputBufferSize);
}

bool NativeLatencyMeasurement::measureAudioLatency() {
  //  ALOGV("measureAudioLatency in");
    int amplitudeSamples = 1;
    auto avgDataAmplitude = getAmplitudeSum(amplitudeSamples);
    auto avgAmplitude = avgDataAmplitude.first / amplitudeSamples;

    bool thresholdFound = false;
    if (avgAmplitude > m_noiseAmplitudeThreshold /*+20db*/) { // We've caught output wave.
     //   ALOGV("in");
        int samplesCount = 0;
        for (int i = 0; i < m_inputBufferSize; i++) {
            samplesCount++;
            if (static_cast<int>(fabs(m_inputBuffer[i])) > m_noiseAmplitudeThreshold) {
                thresholdFound = true;
                break;
            }
        }

        m_totalSamplesReceived += samplesCount;
        ALOGV("out avgamplitude > threshold");
    } else {
        m_totalSamplesReceived += amplitudeSamples;
      // __android_log_print(ANDROID_LOG_INFO, "measure", "samples = %d", (int)m_totalSamplesReceived);
    }
    //__android_log_print(ANDROID_LOG_INFO, "Threshold found", "samples = %d", (int)m_totalSamplesReceived);
  //  ALOGV("measureAudioLatency STEP");
    if (thresholdFound) {
        computeAudioLatency();
    }
   // ALOGV("measureAudioLatency out");
    return thresholdFound;
}

void NativeLatencyMeasurement::measureEnvironmentNoise() {
    int amplitudeSamples = 1;
    auto avgDataAmplitude = getAmplitudeSum(amplitudeSamples);
    auto avgAmplitude = avgDataAmplitude.first / amplitudeSamples;

    m_avgAmplitude = (m_avgAmplitude + avgAmplitude) / 2;
    m_totalSamplesReceived += amplitudeSamples;

    if (m_totalSamplesReceived >= m_sampleRate ) {
        ALOGV("Mersure ennoise DONE!!!!");
        m_environmentNoiseChecked = true;

        m_environmentNoise = amplitude_to_db(m_avgAmplitude) + 20.0; // +20db
        m_noiseAmplitudeThreshold = static_cast<int>(pow(10.0, m_environmentNoise / 20.0) * 32767.0);
        m_totalSamplesReceived = 0; // Now we use this member variable to measure latency.
        m_totalCallbacks = 0;


        cv.notify_all();
    }
}

void NativeLatencyMeasurement::processMeasurement(SLAndroidSimpleBufferQueueItf itf, void *context) {
    auto stopEnqueue = false;
  //  ALOGV("ENQUEUE IN =");
    if (not m_environmentNoiseChecked) {
        measureEnvironmentNoise();
    } else {
        stopEnqueue = measureAudioLatency();
    }
 //   ALOGV("ENQUEUE STEP 2");
    //pthread_mutex_unlock(&mutex);
    if (!stopEnqueue) {
     //   pthread_mutex_unlock(&mutex);
   //     ALOGV("ENQUEUE OUT ENQUEUE");
       auto res = (*itf)->Enqueue(itf, m_inputBuffer, m_inputBufferSize*2);
        if (res != SL_RESULT_SUCCESS) {
            res = (*itf)->Enqueue(itf, m_inputBuffer, m_inputBufferSize*2);
            __android_log_print(ANDROID_LOG_INFO, "processMearement", "res = %d", res);
        }

    }
   // ALOGV("ENQUEUE OUT2");
}

void NativeLatencyMeasurement::triggerJNIPlayRequestCallback() {
    ALOGV("PLAY REQUEST");
    m_playRequestCallback();
}

void NativeLatencyMeasurement::triggerJNICallback(int latency) {
    m_jniCallback(latency);
}

void NativeLatencyMeasurement::computeAudioLatency() {
    __android_log_print(ANDROID_LOG_INFO, "LATENCY catched! callbacks = ", "%d", self->m_totalCallbacks);
    __android_log_print(ANDROID_LOG_INFO, "LATENCY catched! samplerate = ", "%d", m_sampleRate);
    __android_log_print(ANDROID_LOG_INFO, "LATENCY catched! samplesreceived = ", "%d", m_totalSamplesReceived);

    double samplesReceived = static_cast<double>(m_totalSamplesReceived); // channel count
    double sampleRate = static_cast<double>(m_sampleRate);
    int latency = static_cast<int>((samplesReceived / sampleRate) * 1000.0);

    std::thread t([=] () {
        triggerJNICallback(latency);
    });
    t.detach();
}


