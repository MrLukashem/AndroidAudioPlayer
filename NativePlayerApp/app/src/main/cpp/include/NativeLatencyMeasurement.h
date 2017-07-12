//
// Created by MrLukashem on 22.04.2017.
//

#ifndef CUSTOMPLAYER_NATIVELATENCYMEASUREMENT_H
#define CUSTOMPLAYER_NATIVELATENCYMEASUREMENT_H

#include <memory>

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

class NativeLatencyMeasurement {
private:
	double m_environmentNoise;
    int m_noiseAmplitudeThreshold = 0;

	int m_bitDepth;
	int m_sampleRate;

    int m_avgAmplitude = 0;
    SLuint32 m_totalSamplesReceived = 0;

	SLObjectItf m_objectItf; 
	SLEngineItf m_engineItf;
	SLObjectItf m_recorderBufferQueue;

	short* m_inputBuffer;
	SLuint32 m_inputBufferSize;

	int initialize();

	virtual void deinitOpenSL();
public:
	NativeLatencyMeasurement(std::function<void(int)>, std::function<void(void)>);
	virtual ~NativeLatencyMeasurement();

	int startTest(int /*BitDepth*/, int /*SampleRate*/);

	void processMeasurement(SLAndroidSimpleBufferQueueItf, void*);

	int m_totalCallbacks = 0;
protected:
	const int UN_INITIALIZED = -1;

	bool m_environmentNoiseChecked = false;

    std::function<void(int)> m_jniCallback;
    std::function<void(void)> m_playRequestCallback;

	void measureEnvironmentNoise();

	int getMicDeviceID();

	SLuint32 initializeRecorder();

	SLuint32 startRecording();

	bool measureAudioLatency();

    void computeAudioLatency();

    void triggerJNICallback(int);

    void triggerJNIPlayRequestCallback();

    std::pair<int, int> getAmplitudeSum(int&);
};


#endif //CUSTOMPLAYER_NATIVELATENCYMEASUREMENT_H
