#pragma once
#include <atomic>

struct ESPColor { uint8_t r, g, b, a; };

struct State {
    std::atomic<bool>  aim_enabled     {false};
    std::atomic<bool>  aim_on_scope    {true};
    std::atomic<bool>  aim_on_fire     {true};
    std::atomic<bool>  aim_ignore_bots {true};
    std::atomic<float> aim_fov         {80.f};
    std::atomic<float> aim_smooth      {5.f};
    std::atomic<int>   aim_bone        {0};
    // comportamento humano (reduz ban via telemetria behavioral)
    std::atomic<float> aim_miss_rate       {0.30f};  // 0..0.5 — % tiros errados
    std::atomic<bool>  aim_bone_random     {true};   // sorteia osso a cada tick
    std::atomic<int>   aim_reaction_ms     {200};    // atraso 100..500 ms
    std::atomic<int>   aim_kpm_cap         {8};      // kills/min cap (0=off)
    std::atomic<int>   aim_max_hs_streak   {3};      // forca peito apos N HS
    std::atomic<bool>  no_recoil       {true};
    std::atomic<bool>  no_spread       {false};
    std::atomic<bool>  fast_fire       {false};
    std::atomic<bool>  instant_reload  {false};
    std::atomic<bool>  magic_bullet    {false};
    std::atomic<bool>  wide_view       {false};
    std::atomic<float> fov_mult        {1.3f};

    std::atomic<bool>  esp_enabled     {true};
    std::atomic<bool>  esp_box         {true};
    std::atomic<bool>  esp_skeleton    {true};
    std::atomic<bool>  esp_name        {false};
    std::atomic<bool>  esp_distance    {true};
    std::atomic<bool>  esp_health      {true};
    std::atomic<bool>  esp_snapline    {false};
    std::atomic<bool>  esp_head_dot    {true};
    std::atomic<bool>  esp_bot_tag     {true};
    std::atomic<float> esp_max_dist    {200.f};
    ESPColor esp_enemy = {0, 255, 180, 255};
    ESPColor esp_bot   = {120, 120, 120, 200};
    ESPColor esp_head  = {255, 0, 128, 255};

    std::atomic<bool>  speed_hack      {false};
    std::atomic<float> speed_mult      {1.6f};
    std::atomic<bool>  low_gravity     {false};
    std::atomic<float> gravity_mult    {0.4f};
    std::atomic<bool>  fly_mode        {false};
    std::atomic<bool>  bhop            {false};
    std::atomic<bool>  teleport_loot   {false};

    std::atomic<bool>  auto_loot       {false};
    std::atomic<bool>  gloo_spam       {false};
    std::atomic<bool>  medkit_spam     {false};
    std::atomic<bool>  ammo_infinite   {false};
    std::atomic<bool>  radar_full      {true};
    std::atomic<bool>  rank_protect    {false};

    std::atomic<bool>  panel_hidden    {false};
    std::atomic<bool>  flag_secure     {true};
    std::atomic<bool>  panic           {false};
};

inline State g_state;
