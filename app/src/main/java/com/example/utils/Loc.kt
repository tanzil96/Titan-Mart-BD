package com.example.utils

object Loc {
    private val translations = mapOf(
        "app_title" to Pair("Titan Mart BD SMS Sender", "টাইটান মার্ট বিডি এসএমএস প্রেরক"),
        "nav_dashboard" to Pair("Dashboard", "ড্যাশবোর্ড"),
        "nav_customers" to Pair("Customers", "গ্রাহক তালিকা"),
        "nav_templates" to Pair("Templates", "টেমপ্লেট"),
        "nav_send" to Pair("SMS Sending", "এসএমএস পাঠান"),
        "nav_reports" to Pair("Reports", "রিপোর্ট ও লগ"),
        "nav_settings" to Pair("Settings", "সেটিংস"),
        
        // Dashboard
        "db_total_customers" to Pair("Total Customers", "মোট গ্রাহক সংখ্যা"),
        "db_sms_sent" to Pair("SMS Sent", "এসএমএস পাঠানো হয়েছে"),
        "db_sms_failed" to Pair("SMS Failed", "এসএমএস ব্যর্থ হয়েছে"),
        "db_pending" to Pair("Pending", "অপেক্ষমাণ"),
        "db_today_sms" to Pair("Today's SMS", "আজকের এসএমএস"),
        "db_subtitle" to Pair("Offline SIM & WhatsApp Sender", "অফলাইন সিম এবং হোয়াটসঅ্যাপ প্রেরক"),
        "db_quick_actions" to Pair("Quick Actions", "দ্রুত কার্যসমূহ"),

        // Customer List
        "cust_search_placeholder" to Pair("Search by Name, Phone or Order ID...", "নাম, ফোন অথবা অর্ডার আইডি দিয়ে খুঁজুন..."),
        "cust_filter" to Pair("Filter Status", "ফিল্টার করুন"),
        "cust_import" to Pair("Import Customer Data", "গ্রাহকের তথ্য ইম্পোর্ট"),
        "cust_add_manual" to Pair("Add Manually", "ম্যানুয়ালি যোগ করুন"),
        "cust_import_contacts" to Pair("Import Contacts", "কন্টাক্ট ইম্পোর্ট"),
        "cust_pasted_text" to Pair("Paste Excel/CSV Text", "টেক্সট পেস্ট করে ইম্পোর্ট"),
        "cust_selected" to Pair("Selected", "নির্বাচিত"),
        "cust_select_all" to Pair("Select All", "সব সিলেক্ট করুন"),
        "cust_clear_selection" to Pair("Clear Selection", "সিলেকশন মুছুন"),
        "cust_empty" to Pair("No customers found. Import some data to begin!", "কোন গ্রাহক পাওয়া যায়নি। শুরু করতে ডাটা ইম্পোর্ট করুন!"),
        
        // Templates
        "temp_sms_title" to Pair("Default SMS Template", "ডিফল্ট এসএমএস টেমপ্লেট"),
        "temp_wa_title" to Pair("Default WhatsApp Template", "ডিফল্ট হোয়াটসঅ্যাপ টেমপ্লেট"),
        "temp_variables" to Pair("Insert Variables", "ভেরিয়েবল যুক্ত করুন"),
        "temp_saved" to Pair("Template saved successfully!", "টেমপ্লেট সফলভাবে সংরক্ষিত হয়েছে!"),

        // SMS sending
        "send_sim_selection" to Pair("Select SIM Card", "সিম কার্ড নির্বাচন"),
        "send_delay_selection" to Pair("Delay Between SMS", "এসএমএস-এর মধ্যবর্তী বিরতি"),
        "send_progress" to Pair("Sending Progress", "পাঠানোর অগ্রগতি"),
        "send_btn_start" to Pair("Start Sending", "পাঠানো শুরু করুন"),
        "send_btn_pause" to Pair("Pause", "বিরতি দিন"),
        "send_btn_resume" to Pair("Resume", "পুনরায় শুরু"),
        "send_btn_stop" to Pair("Stop", "থামিয়ে দিন"),
        "send_confirm_title" to Pair("Confirm SMS Sending", "এসএমএস পাঠানোর তথ্য নিশ্চিত করুন"),
        "send_confirm_msg" to Pair("Are you sure you want to send SMS to {Count} customers?", "আপনি কি নিশ্চিত যে {Count} জন গ্রাহককে এসএমএস পাঠাতে চান?"),
        
        // WhatsApp support
        "wa_mode" to Pair("Choose Sending Mode", "পাঠানোর মাধ্যম"),
        "wa_mode_sms" to Pair("SMS Only", "শুধু এসএমএস"),
        "wa_mode_wa" to Pair("WhatsApp Only", "শুধু হোয়াটসঅ্যাপ"),
        "wa_mode_both" to Pair("SMS + WhatsApp", "এসএমএস + হোয়াটসঅ্যাপ"),
        "wa_use_business" to Pair("Use WhatsApp Business", "হোয়াটসঅ্যাপ বিজনেস ব্যবহার করুন"),
        "wa_warning" to Pair("Warning", "সতর্কতা"),

        // Reports
        "rep_clear_logs" to Pair("Clear Logs", "লগ পরিষ্কার করুন"),
        "rep_export" to Pair("Export Report", "রিপোর্ট এক্সপোর্ট"),
        "rep_export_xlsx" to Pair("Export to Excel", "এক্সেল ফাইল এক্সপোর্ট"),
        "rep_export_csv" to Pair("Export to CSV", "সিএসভি ফাইল এক্সপোর্ট"),
        "rep_tab_sms" to Pair("SMS History", "এসএমএস ইতিহাস"),
        "rep_tab_wa" to Pair("WhatsApp History", "হোয়াটসঅ্যাপ ইতিহাস"),
        "rep_no_logs" to Pair("No transmission logs recorded yet.", "এখনও কোনো পাঠানোর লগ রেকর্ড করা হয়নি।"),

        // Settings
        "set_dark_mode" to Pair("Appearance Theme", "অ্যাপ থিম"),
        "set_lang" to Pair("App Language", "অ্যাপের ভাষা"),
        "set_def_sim" to Pair("Default SIM Selection", "ডিফল্ট সিম নির্বাচন"),
        "set_def_delay" to Pair("Default Delay", "ডিফল্ট বিরতি সময়"),
        "set_about" to Pair("About Titan Mart BD Sender", "টাইটান মার্ট বিডি প্রেরক সম্পর্কে"),
        "set_about_desc" to Pair("V1.0.0 - Fully Offline & Secure Customer Outreach System.", "ভি১.০.০ - সম্পূর্ণ অফলাইন এবং নিরাপদ গ্রাহক যোগাযোগ ব্যবস্থা।"),

        // Dialogs / General
        "gen_save" to Pair("Save Changes", "পরিবর্তন সংরক্ষণ"),
        "gen_cancel" to Pair("Cancel", "বাতিল"),
        "gen_confirm" to Pair("Confirm", "নিশ্চিত করুন"),
        "gen_success" to Pair("Success", "সফল হয়েছে"),
        "gen_error" to Pair("Error", "ত্রুটি"),
        "gen_close" to Pair("Close", "বন্ধ করুন")
    )

    fun t(key: String, language: String): String {
        val pair = translations[key] ?: return key
        return if (language == "bn") pair.second else pair.first
    }
}
