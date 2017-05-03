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
        short* data;
        int size; // data size
        int bufferSize; // data portion size
    } playbackContext;

    void simpleBufferQueueCallback(SLAndroidSimpleBufferQueueItf itf, void *con) {
        SLuint32 sizeToEnqueue = static_cast<SLuint32>(playbackContext.bufferSize);
        if (playbackContext.size < playbackContext.bufferSize) {
            sizeToEnqueue = static_cast<SLuint32>(playbackContext.size);
            playbackContext.size = 0;
        } else if (playbackContext.size <= 0) {
            return;
        }

        (*itf)->Enqueue(itf, playbackContext.data, static_cast<SLuint32>(sizeToEnqueue));

        playbackContext.data += sizeToEnqueue;
        playbackContext.size -= sizeToEnqueue;
    }
};

NativePlayer::NativePlayer() {
    initialize(BASIC_MODE);

    me = this;
}

NativePlayer::~NativePlayer() {
    if (m_dataSource.pLocator != nullptr) {
        delete reinterpret_cast<SLDataLocator_AndroidSimpleBufferQueue*>(m_dataSource.pLocator);
    }
    if (m_dataSource.pFormat != nullptr) {
        delete reinterpret_cast<SLDataFormat_PCM*>(m_dataSource.pFormat);
    }

    delete m_samplesBufferShort;
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
        short *buffer, int size, int bitsPerSample, int channelCount, int sampleRate, int bufferSize) {
    m_samplesBufferShort = new short[size];
    std::memcpy(m_samplesBufferShort, buffer, size);
    m_inputBufferSize = size;
    m_bufferSize = bufferSize;

    // Input configuration
    SLuint16 containerSize = 0;
    if (bitsPerSample > SL_PCMSAMPLEFORMAT_FIXED_16) {
        containerSize = SL_PCMSAMPLEFORMAT_FIXED_32;
    } else {
        containerSize = static_cast<SLuint16>(bitsPerSample);
    }
    m_sourceMode = SourceMode::BUFFER_SOURCE;

    auto format_pcm = new SLDataFormat_PCM;
    format_pcm->bitsPerSample = static_cast<SLuint32>(bitsPerSample);
    format_pcm->formatType = SL_DATAFORMAT_PCM;
    format_pcm->channelMask = SL_SPEAKER_FRONT_RIGHT;
    format_pcm->numChannels = static_cast<SLuint32>(channelCount);
    format_pcm->containerSize = containerSize;
    format_pcm->endianness = SL_BYTEORDER_LITTLEENDIAN;
    format_pcm->samplesPerSec = static_cast<SLuint32>(sampleRate * 1000);

    auto sourceLocator = new SLDataLocator_AndroidSimpleBufferQueue;
    sourceLocator->numBuffers = static_cast<SLuint32>(channelCount);
    sourceLocator->locatorType = SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE;

    m_dataSource.pFormat = static_cast<void*>(&(*format_pcm));
    m_dataSource.pLocator = static_cast<void*>(&(*sourceLocator));

    // Output configuration
    // Preparing output mix
    SLObjectItf outputMix;
    auto res = (*m_EngineItf)->CreateOutputMix(m_EngineItf, &outputMix, 0, m_idsArray, m_required);
    if (res != SL_RESULT_SUCCESS) {
        ALOGV("CreateOutputMix error");
        return;
    }
    res = (*outputMix)->Realize(outputMix, SL_BOOLEAN_FALSE);
    if (res != SL_RESULT_SUCCESS) {
        ALOGV("Realize CreateOutputMix error");
        return;
    }
    auto outputMixLocatorLocator = new SLDataLocator_OutputMix;
    outputMixLocatorLocator->locatorType = SL_DATALOCATOR_OUTPUTMIX;
    outputMixLocatorLocator->outputMix = outputMix;

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
                m_EngineItf, &playerItf, &m_dataSource, &m_sink, 1, m_idsArray, m_required);
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*playerItf)->Realize(playerItf, SL_BOOLEAN_FALSE);
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*playerItf)->GetInterface(
                playerItf, SL_IID_PLAY, static_cast<void*>(&m_playbackItf));
        if (res != SL_RESULT_SUCCESS) {
            // TODO: Handle an error
            ALOGV("The engine has been created");
        }

        res = (*playerItf)->GetInterface(
                playerItf, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
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

        playbackContext.data = m_samplesBufferShort;
        playbackContext.bufferSize = m_bufferSize;
        playbackContext.size = m_inputBufferSize;
    }
}

int NativePlayer::play(int dataID) {
    (*m_simpleAndroidBufferQueue)->Enqueue(m_simpleAndroidBufferQueue, m_samplesBufferShort,
                                           static_cast<SLuint32>(m_bufferSize));
    playbackContext.data += m_bufferSize;
    playbackContext.size -= m_inputBufferSize - m_bufferSize;

    auto res = (*m_playbackItf)->SetPlayState(m_playbackItf, SL_PLAYSTATE_PLAYING);
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
}

void NativePlayer::emptyBufferCallback() {
    auto res = (*m_playbackItf)->SetPlayState(m_playbackItf, SL_PLAYSTATE_STOPPED);
    if (res != SL_RESULT_SUCCESS) {
        // TODO: Handle error
        ALOGV("The engine has been created");
    }
}

















