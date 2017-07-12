//
// Created by MrLukashem on 19.03.2017.
//

#include "include/NativePlayer.h"
#include "include/common.h"

#include <cstring>
#include <sstream>

namespace {
    auto to_string = [](auto value) -> std::string {
        std::ostringstream ss;
        ss << value;

        return ss.str();
    };

    NativePlayer* me = nullptr;

    struct PlaybackContext {
        void* data;
        SLuint32 size; // data size // (IN BYTES!)
        SLuint32 bufferSize; // data portion size // (IN BYTES!)
    } playbackContext;

    void simpleBufferQueueCallback(SLAndroidSimpleBufferQueueItf itf, void *con) {
        SLuint32 sizeToEnqueue = playbackContext.bufferSize;
        if (playbackContext.size <= playbackContext.bufferSize) {
            sizeToEnqueue = playbackContext.size;
            playbackContext.size = 0;
        } else if (playbackContext.size <= 0) {
            //delete me;
            return;
        }

        auto res = (*itf)->Enqueue(itf, playbackContext.data, static_cast<SLuint32>(sizeToEnqueue));
        if (res != SL_RESULT_SUCCESS) {
            ALOGV("ENQUEUE ERROR");
        }

        playbackContext.data = (static_cast<uint8_t*>(playbackContext.data) + sizeToEnqueue);
        playbackContext.size -= sizeToEnqueue;
    }
};

NativePlayer::NativePlayer() {
    initialize(BASIC_MODE);

    me = this;
}

NativePlayer::~NativePlayer() {
    ALOGV("~NativePlayer() IN 3");

}

int NativePlayer::initialize(int mode) {
    switch (mode) {
        case BASIC_MODE:
            break;
        case CALLBACK_MODE:
            break;
        case POOL_MODE:
            break;
        default:
            break;
    }

    auto res = slCreateEngine(&m_EngineObjectItf, 0, nullptr, 0, nullptr, nullptr);
    ALOGV("The engine has been created");

    res = (*m_EngineObjectItf)->GetInterface(m_EngineObjectItf, SL_IID_ENGINE, &m_EngineItf);

    for (int i = 0; i < 3; i++) {
        m_required[i] = SL_BOOLEAN_FALSE;
        m_idsArray[i] = SL_IID_NULL;
    }

    return res;
}

void NativePlayer::setBufferSource(
        void *buffer, int size, int bitsPerSample, int channelCount, int sampleRate, int bufferSize,
        bool isFloat) {
    ALOGV("setBufferSource")
    m_bufferSize = static_cast<SLuint32>(bufferSize);
    m_sourceMode = SourceMode::BUFFER_SOURCE;

    if (!isFloat) {
        auto format_pcm = new SLDataFormat_PCM;
        SLuint16 containerSize = 0;

        // Input configuration
        if (bitsPerSample > SL_PCMSAMPLEFORMAT_FIXED_16) {
            containerSize = SL_PCMSAMPLEFORMAT_FIXED_32;
        } else if (bitsPerSample == SL_PCMSAMPLEFORMAT_FIXED_16) {
            containerSize = SL_PCMSAMPLEFORMAT_FIXED_16;
            m_samplesBufferShort = new short[size];
        } else if (bitsPerSample == SL_PCMSAMPLEFORMAT_FIXED_8) {
            containerSize = SL_PCMSAMPLEFORMAT_FIXED_8;
            m_samplesBufferShort = new uint8_t[size];
        }
        auto bytesInContainer = static_cast<SLuint32>(static_cast<float>(containerSize) / 8.0);
        m_alignedFrameSize = containerSize;
        m_inputBufferSize = size * bytesInContainer;
        __android_log_print(ANDROID_LOG_INFO, "containersize", "inpusize = %d", (int)m_inputBufferSize);

        format_pcm->bitsPerSample = static_cast<SLuint32>(bitsPerSample);
        format_pcm->formatType = SL_DATAFORMAT_PCM;
        format_pcm->numChannels = static_cast<SLuint32>(channelCount);
        format_pcm->containerSize = containerSize;
        format_pcm->endianness = SL_BYTEORDER_LITTLEENDIAN;
        format_pcm->samplesPerSec = static_cast<SLuint32>(sampleRate * 1000);

        if (channelCount == 1) {
            format_pcm->channelMask = SL_SPEAKER_FRONT_RIGHT;
        } else {
            format_pcm->channelMask = SL_SPEAKER_FRONT_RIGHT | SL_SPEAKER_FRONT_LEFT;
        }

        m_dataSource.pFormat = static_cast<void*>(&(*format_pcm));
    } else {
        m_alignedFrameSize = 32;
        m_inputBufferSize = size * static_cast<SLuint32>(32);

        auto format_pcm = new SLAndroidDataFormat_PCM_EX;
        format_pcm->bitsPerSample = 32;
        format_pcm->formatType = SL_ANDROID_DATAFORMAT_PCM_EX;
        format_pcm->channelMask = SL_SPEAKER_FRONT_RIGHT;
        format_pcm->numChannels = static_cast<SLuint32>(channelCount);
        format_pcm->containerSize = 32; //32 bits
        format_pcm->endianness = SL_BYTEORDER_LITTLEENDIAN;
        format_pcm->sampleRate = static_cast<SLuint32>(sampleRate * 1000);
        format_pcm->representation = SL_ANDROID_PCM_REPRESENTATION_FLOAT;

        if (channelCount == 1) {
            format_pcm->channelMask = SL_SPEAKER_FRONT_RIGHT;
        } else {
            format_pcm->channelMask = SL_SPEAKER_FRONT_RIGHT | SL_SPEAKER_FRONT_LEFT;
        }

        m_dataSource.pFormat = static_cast<void*>(&(*format_pcm));
    }

    // copy buffer bytes to local memory
    std::memcpy(m_samplesBufferShort, buffer, static_cast<size_t>(m_inputBufferSize));

    if (bitsPerSample == SL_PCMSAMPLEFORMAT_FIXED_8) {
        int8_t* bytesSamples = static_cast<int8_t *>(m_samplesBufferShort);
        for (int i = 0; i < size; i++) {
            // save 8bit signed value on 16 bit signed buffer.
            int16_t temp = bytesSamples[i];
            // change range from -128 : 127 to 0 : 256.
            temp += 128;

            // get 8bit unsigned representation of bytes's array element.
            uint8_t* bytesUSamples = static_cast<uint8_t*>(m_samplesBufferShort);
            uint8_t* source_ptr = &bytesUSamples[i];
            // insert into memory value from range 0 : 256 to unsigned 8bit element.
            *source_ptr = static_cast<uint8_t>(temp);
        }
    }

    auto sourceLocator = new SLDataLocator_AndroidSimpleBufferQueue;
    sourceLocator->numBuffers = static_cast<SLuint32>(channelCount);
    sourceLocator->locatorType = SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE;
    m_dataSource.pLocator = static_cast<void*>(&(*sourceLocator));

    // Output configuration
    // Preparing output mix
    auto res = (*m_EngineItf)->CreateOutputMix(m_EngineItf, &m_outputMix, 0, m_idsArray, m_required);
    if (res != SL_RESULT_SUCCESS) {
        ALOGV("CreateOutputMix error");
        return;
    }
    res = (*m_outputMix)->Realize(m_outputMix, SL_BOOLEAN_FALSE);
    if (res != SL_RESULT_SUCCESS) {
        ALOGV("Realize CreateOutputMix error");
        return;
    }
    auto outputMixLocatorLocator = new SLDataLocator_OutputMix;
    outputMixLocatorLocator->locatorType = SL_DATALOCATOR_OUTPUTMIX;
    outputMixLocatorLocator->outputMix = m_outputMix;

    m_sink.pLocator = static_cast<void*>(&(*outputMixLocatorLocator));
    m_sink.pFormat = nullptr;
}

bool NativePlayer::validatePathString(const std::string &path) {
    if (path.empty()) {
        return ERROR_CODE;
    }

    return true;
}

int NativePlayer::load(const std::string &path) {
    if (not validatePathString(path)) {
        return ERROR_CODE;
    }

    m_SongsVec.push_back(std::move(SongPair{path, ++m_SongsCounter}));
    return m_SongsCounter;
}

void NativePlayer::pause() {

}

void NativePlayer::prepare() {

    if (m_sourceMode == SourceMode::BUFFER_SOURCE) { // || == DATA_SOURCE && PCM_FORMAT
        SLObjectItf playerItf;
        m_idsArray[0] = SL_IID_ANDROIDSIMPLEBUFFERQUEUE;
        m_required[0] = SL_BOOLEAN_TRUE;

        auto res = (*m_EngineItf)->CreateAudioPlayer(
                m_EngineItf, &m_PlayerItf, &m_dataSource, &m_sink, 1, m_idsArray, m_required);
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*m_PlayerItf)->Realize(m_PlayerItf, SL_BOOLEAN_FALSE);
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*m_PlayerItf)->GetInterface(
                m_PlayerItf, SL_IID_PLAY, static_cast<void*>(&m_playbackItf));
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*m_PlayerItf)->GetInterface(
                m_PlayerItf, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                static_cast<void*>(&m_simpleAndroidBufferQueue));
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*m_simpleAndroidBufferQueue)->RegisterCallback(m_simpleAndroidBufferQueue,
                                                              &simpleBufferQueueCallback,
                                                              nullptr);
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        SLuint32 frameSizeInBytes = static_cast<SLuint32>(static_cast<double>(m_alignedFrameSize) / 8.0);
        auto bufferSizeInBytes =
                frameSizeInBytes /*Frame size in bytes*/ *
                m_bufferSize; // Buffer size in Frames
        playbackContext.data = m_samplesBufferShort;
        playbackContext.bufferSize = bufferSizeInBytes;
        playbackContext.size = m_inputBufferSize;
    }
}

int NativePlayer::play(int dataID) {
    SLuint32 frameSizeInBytes = static_cast<SLuint32>(static_cast<double>(m_alignedFrameSize) / 8.0);
    auto bufferSizeInBytes =
            frameSizeInBytes /*Frame size in bytes*/ *
            m_bufferSize; // Buffer size in Frames
    auto res = (*m_simpleAndroidBufferQueue)->Enqueue(m_simpleAndroidBufferQueue, m_samplesBufferShort,
                                                      bufferSizeInBytes);
    if (res != SL_RESULT_SUCCESS) {
        ALOGV("ENQUEUE ERROR");
    }

    playbackContext.data = static_cast<void*>(static_cast<uint8_t*>(playbackContext.data)
                                              + (bufferSizeInBytes));
    playbackContext.size -= bufferSizeInBytes;

    res = (*m_playbackItf)->SetPlayState(m_playbackItf, SL_PLAYSTATE_PLAYING);
    if (res != SL_RESULT_SUCCESS) {
        // TODO: Handle an error
        ALOGV("The engine has been created");
    }

    return 0;
}

SongsVec NativePlayer::getSongs() {
    return SongsVec{};
}

void NativePlayer::seekTo(int ms) {
    ALOGV("The engine has been created");
}

int NativePlayer::getCurrentPosition() {
    return 0;
}

void NativePlayer::stop() {
    ALOGV("The engine has been created");
}

void NativePlayer::release() {
    ALOGV("The engine has been created");
    if (m_alignedFrameSize == 8) {
        delete [] static_cast<uint8_t*>(m_samplesBufferShort);
    } else if (m_alignedFrameSize == 16) {
        delete [] static_cast<short*>(m_samplesBufferShort);
    } else if (m_alignedFrameSize == 32) {
        delete [] static_cast<float*>(m_samplesBufferShort);
    }

    (*m_playbackItf)->SetPlayState(m_playbackItf, SL_PLAYSTATE_STOPPED);
    (*m_simpleAndroidBufferQueue)->Clear(m_simpleAndroidBufferQueue);
    ALOGV("~NativePlayer() IN 4");
    (*m_PlayerItf)->Destroy(m_PlayerItf);
    (*m_outputMix)->Destroy(m_outputMix);
    ALOGV("~NativePlayer() IN 5");

    ALOGV("~NativePlayer() IN 6");
    (*m_EngineObjectItf)->Destroy(m_EngineObjectItf);
    ALOGV("~NativePlayer() OUT");

    ALOGV("~NativePlayer() IN");
    if (m_dataSource.pLocator != nullptr) {
        delete reinterpret_cast<SLDataLocator_AndroidSimpleBufferQueue*>(m_dataSource.pLocator);
    }

    ALOGV("~NativePlayer() IN 2" );
    if (m_dataSource.pFormat != nullptr) {
        delete reinterpret_cast<SLDataFormat_PCM*>(m_dataSource.pFormat);
    }
}

void NativePlayer::emptyBufferCallback() {
    auto res = (*m_playbackItf)->SetPlayState(m_playbackItf, SL_PLAYSTATE_STOPPED);
    if (res != SL_RESULT_SUCCESS) {
        // TODO: Handle error
        ALOGV("The engine has been created");
    }
}
