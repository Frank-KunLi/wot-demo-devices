#include <string.h>
#include "iot_timer.h"
#include "sdk_common.h"
#include "iot_errors.h"

/**
 * @defgroup api_param_check API Parameters check macros.
 *
 * @details Macros that verify parameters passed to the module in the APIs. These macros
 *          could be mapped to nothing in final versions of code to save execution and size.
 *          IOT_TIMER_DISABLE_API_PARAM_CHECK should be defined to disable these checks.
 *
 * @{
 */
#if (IOT_TIMER_DISABLE_API_PARAM_CHECK == 0)

/**
 * @brief Verify NULL parameters are not passed to API by application.
 */
#define NULL_PARAM_CHECK(PARAM)                                                                    \
        if ((PARAM) == NULL)                                                                       \
        {                                                                                          \
            return (NRF_ERROR_NULL | IOT_TIMER_ERR_BASE);                                          \
        }

#define VERIFY_CLIENT_LIST_IS_VALID(PARAM)                                                         \
        if ((PARAM) != NULL)                                                                       \
        {                                                                                          \
            uint8_t i;                                                                             \
            for (i = 0; i < (PARAM)->client_list_length; i++)                                      \
            {                                                                                      \
                if (((PARAM)->p_client_list[i].iot_timer_callback) == NULL)                        \
                {                                                                                  \
                    return (NRF_ERROR_INVALID_PARAM | IOT_TIMER_ERR_BASE);                         \
                }                                                                                  \
                if (((PARAM)->p_client_list[i].cb_interval == 0)                          ||       \
                    ((PARAM)->p_client_list[i].cb_interval < IOT_TIMER_RESOLUTION_IN_MS)  ||       \
                    (((PARAM)->p_client_list[i].cb_interval % IOT_TIMER_RESOLUTION_IN_MS) != 0))   \
                {                                                                                  \
                    return (NRF_ERROR_INVALID_PARAM | IOT_TIMER_ERR_BASE);                         \
                }                                                                                  \
            }                                                                                      \
        }

#define VERIFY_WALL_CLOCK_VALUE_IS_VALID(PARAM)                                                    \
        if ((PARAM) != NULL)                                                                       \
        {                                                                                          \
            if ((*PARAM % IOT_TIMER_RESOLUTION_IN_MS) != 0)                                        \
            {                                                                                      \
                return (NRF_ERROR_INVALID_PARAM | IOT_TIMER_ERR_BASE);                             \
            }                                                                                      \
        }

/**
 * @brief Verify NULL parameters are not passed to API by application.
 */
#define NULL_PARAM_CHECK(PARAM)                                                                    \
        if ((PARAM) == NULL)                                                                       \
        {                                                                                          \
            return (NRF_ERROR_NULL | IOT_TIMER_ERR_BASE);                                          \
        }

#else // IOT_TIMER_DISABLE_API_PARAM_CHECK

#define NULL_PARAM_CHECK(PARAM)
#define VERIFY_CLIENT_LIST_IS_VALID(PARAM)
#define VERIFY_WALL_CLOCK_VALUE_IS_VALID(PARAM)

#endif //IOT_TIMER_DISABLE_API_PARAM_CHECK
/** @} */

static iot_timer_time_in_ms_t           m_wall_clock = 0;
static const iot_timer_clients_list_t * m_clients    = NULL;


uint32_t iot_timer_client_list_set(const iot_timer_clients_list_t * p_list_of_clients)
{
    VERIFY_CLIENT_LIST_IS_VALID(p_list_of_clients);

    m_clients = p_list_of_clients;
    return NRF_SUCCESS;
}


uint32_t iot_timer_update(void)
{
    m_wall_clock += IOT_TIMER_RESOLUTION_IN_MS;
    if ((0xFFFFFFFFUL - m_wall_clock) < IOT_TIMER_RESOLUTION_IN_MS)
    {
        m_wall_clock = IOT_TIMER_RESOLUTION_IN_MS;
    }
    if (m_clients != NULL)
    {
        uint8_t index;
        for (index = 0; index < m_clients->client_list_length; index++)
        {
            if ((m_wall_clock % m_clients->p_client_list[index].cb_interval) == 0)
            {
                m_clients->p_client_list[index].iot_timer_callback(m_wall_clock);
            }
        }
    }
    return NRF_SUCCESS;
}


uint32_t iot_timer_wall_clock_get(iot_timer_time_in_ms_t * p_elapsed_time)
{
    NULL_PARAM_CHECK(p_elapsed_time);

    *p_elapsed_time = m_wall_clock;
    return NRF_SUCCESS;
}


uint32_t iot_timer_wall_clock_delta_get(iot_timer_time_in_ms_t * p_past_time, \
                                        iot_timer_time_in_ms_t * p_delta_time)
{
    NULL_PARAM_CHECK(p_past_time);
    NULL_PARAM_CHECK(p_delta_time);
    VERIFY_WALL_CLOCK_VALUE_IS_VALID(p_past_time);

    if (*p_past_time == m_wall_clock)
    {
        *p_delta_time = 0;
    }
    else if (*p_past_time < m_wall_clock)
    {
        *p_delta_time = m_wall_clock - *p_past_time;
    }
    else
    {
        // An integer overflow of the wall clock occured since *p_past_time.

        iot_timer_time_in_ms_t max_wall_clock = (0xFFFFFFFFUL / IOT_TIMER_RESOLUTION_IN_MS) \
                                                    * IOT_TIMER_RESOLUTION_IN_MS;
        *p_delta_time = max_wall_clock - *p_past_time; // Before overflow.
        *p_delta_time += m_wall_clock;                 // After overflow. 
        *p_delta_time -= IOT_TIMER_RESOLUTION_IN_MS;   // Because of handling of wall clock integer overflow, see above. 
    } 

    return NRF_SUCCESS;
}