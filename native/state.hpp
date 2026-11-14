#pragma once
#include <atomic>

struct ESPColor { uint8_t r, g, b, a; };

struct State {
    // ===== AIM =====
    std::atomic<bool>  aim_enabled     {false};
    std::atomic<bool>  aim_on_scope    {true};
    std::atomic<bool>  aim_on_fire     {true};
    std::atomic<bool>  aim_ignore_bots {true};
    std::atomic<float> aim_fov         {80.f};
    std::atomic<float> aim_smooth      {5.f};
    std::atomic<int>   aim_bone        {0};
    std::atomic<int>   aim_reaction_ms {200};
    std::atomic<int>   shot_head_count {3};
    std::atomic<int>   shot_body_count {1};
    std::atomic<bool>  aim_prediction  {true};
    std::atomic<bool>  aim_multi_target{true};
    std::atomic<bool>  aim_silent      {false};

    // ===== ESP =====
    std::atomic<int>   esp_mode        {0};      // 0=off 1=legit 2=full 3=audio
    std::atomic<bool>  esp_enabled     {false};  // compat flag (= esp_mode != 0)
    std::atomic<bool>  esp_box         {true};
    std::atomic<bool>  esp_skeleton    {false};
    std::atomic<bool>  esp_name        {false};
    std::atomic<bool>  esp_distance    {true};
    std::atomic<bool>  esp_health      {true};
    std::atomic<bool>  esp_snapline    {false};
    std::atomic<bool>  esp_head_dot    {true};
    std::atomic<bool>  esp_bot_tag     {true};
    std::atomic<float> esp_max_dist    {200.f};
    std::atomic<int>   esp_delay_ms    {0};
    std::atomic<bool>  esp_hide_scope  {true};
    std::atomic<float> esp_audio_thr   {50.f};
    ESPColor esp_enemy = {0, 255, 180, 255};
    ESPColor esp_bot   = {120, 120, 120, 200};
    ESPColor esp_head  = {255, 0, 128, 255};

    // ===== WEAPON (patches memoria) =====
    std::atomic<bool>  no_recoil       {true};
    std::atomic<bool>  no_spread       {false};
    std::atomic<bool>  fast_fire       {false};
    std::atomic<bool>  no_reload       {false};
    std::atomic<bool>  infinite_ammo   {false};
    std::atomic<bool>  one_shot_hs     {false};
    std::atomic<bool>  any_gun_awm     {false};
    std::atomic<bool>  pierce_walls    {false};
    std::atomic<bool>  magic_bullet    {false};
    std::atomic<bool>  instant_reload  {false};  // alias legacy

    // ===== MOVEMENT =====
    std::atomic<bool>  speed_hack      {false};
    std::atomic<float> speed_mult      {1.3f};
    std::atomic<bool>  fly_mode        {false};
    std::atomic<bool>  no_fall_dmg     {true};
    std::atomic<bool>  super_jump      {false};
    std::atomic<bool>  bhop            {false};
    std::atomic<bool>  water_breath    {true};
    std::atomic<bool>  instant_revive  {false};
    std::atomic<bool>  no_collision    {false};
    std::atomic<bool>  low_gravity     {false};
    std::atomic<float> gravity_mult    {0.4f};
    std::atomic<bool>  teleport_loot   {false};
    std::atomic<bool>  wide_view       {false};
    std::atomic<float> fov_mult        {1.3f};

    // ===== INTEL =====
    std::atomic<bool>  grenade_alert   {true};
    std::atomic<bool>  loot_esp        {false};
    std::atomic<bool>  zone_timer      {true};
    std::atomic<bool>  vehicle_esp     {false};
    std::atomic<bool>  minimap_enemy   {false};
    std::atomic<bool>  airdrop_tracker {false};
    std::atomic<bool>  killfeed_spy    {false};
    std::atomic<bool>  radar_full      {true};

    // ===== SAFETY =====
    std::atomic<bool>  spectator_warn  {true};
    std::atomic<bool>  streamer_mode   {false};
    std::atomic<int>   auto_off_kills  {0};
    std::atomic<bool>  hs_cap_dynamic  {true};
    std::atomic<bool>  replay_sanit    {true};
    std::atomic<bool>  signature_rot   {false};

    // ===== MISC / SYS =====
    std::atomic<bool>  auto_loot       {false};
    std::atomic<bool>  gloo_spam       {false};
    std::atomic<bool>  medkit_spam     {false};
    std::atomic<bool>  rank_protect    {false};
    std::atomic<bool>  panel_hidden    {false};
    std::atomic<bool>  flag_secure     {true};
    std::atomic<bool>  panic           {false};
};

inline State g_state;
