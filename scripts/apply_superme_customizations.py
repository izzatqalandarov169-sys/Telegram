from pathlib import Path

path = Path('TMessagesProj/src/main/java/org/telegram/ui/ProfileActivity.java')
s = path.read_text(encoding='utf-8')

def replace_once(old, new):
    global s
    count = s.count(old)
    if count != 1:
        raise SystemExit(f'Expected exactly one match, got {count}: {old[:120]!r}')
    s = s.replace(old, new, 1)

# Custom row declarations.
replace_once(
    '    private int addToGroupInfoRow;\n    private int premiumRow;\n',
    '    private int addToGroupInfoRow;\n    private int adminPanelRow;\n    private int giftCreatorRow;\n    private int giftStoreRow;\n    private int premiumRow;\n'
)

# Initial state.
replace_once(
    '        devicesRow = -1;\n        devicesSectionRow = -1;\n',
    '        devicesRow = -1;\n        devicesSectionRow = -1;\n        adminPanelRow = -1;\n        giftCreatorRow = -1;\n        giftStoreRow = -1;\n'
)

# Click handlers.
replace_once(
    '            } else if (position == setAvatarRow) {\n                onWriteButtonClick();\n            } else if (position == premiumRow) {\n',
    '            } else if (position == setAvatarRow) {\n                onWriteButtonClick();\n            } else if (position == adminPanelRow) {\n                presentFragment(new AdminPanelActivity());\n            } else if (position == premiumRow) {\n'
)
replace_once(
    '            } else if (position == premiumGiftingRow) {\n                UserSelectorBottomSheet.open(0, BirthdayController.getInstance(currentAccount).getState());\n            } else if (position == botPermissionLocation) {\n',
    '            } else if (position == premiumGiftingRow) {\n                UserSelectorBottomSheet.open(0, BirthdayController.getInstance(currentAccount).getState());\n            } else if (position == giftCreatorRow) {\n                presentFragment(new CustomGiftCreatorActivity());\n            } else if (position == giftStoreRow) {\n                presentFragment(new CustomGiftStoreActivity());\n            } else if (position == botPermissionLocation) {\n'
)

# Settings rows.
replace_once(
    '                devicesSectionRow = rowCount++;\n                if (!getMessagesController().premiumFeaturesBlocked()) {\n',
    '                devicesSectionRow = rowCount++;\n                adminPanelRow = rowCount++;\n                if (!getMessagesController().premiumFeaturesBlocked()) {\n'
)
replace_once(
    '                if (!getMessagesController().premiumPurchaseBlocked()) {\n                    premiumGiftingRow = rowCount++;\n                }\n                if (premiumRow >= 0 || starsRow >= 0 || tonRow >= 0 || businessRow >= 0 || premiumGiftingRow >= 0) {\n',
    '                if (!getMessagesController().premiumPurchaseBlocked()) {\n                    premiumGiftingRow = rowCount++;\n                }\n                giftCreatorRow = rowCount++;\n                giftStoreRow = rowCount++;\n                if (premiumRow >= 0 || starsRow >= 0 || tonRow >= 0 || businessRow >= 0 || premiumGiftingRow >= 0 || giftCreatorRow >= 0 || giftStoreRow >= 0) {\n'
)

# Row labels/icons.
replace_once(
    '                    } else if (position == setAvatarRow) {\n                        cellCameraDrawable.setCustomEndFrame(86);\n',
    '                    } else if (position == setAvatarRow) {\n                        cellCameraDrawable.setCustomEndFrame(86);\n'
)
replace_once(
    '                        setAvatarCell = textCell;\n                    } else if (position == addToGroupButtonRow) {\n',
    '                        setAvatarCell = textCell;\n                    } else if (position == adminPanelRow) {\n                        textCell.setTextAndIcon("Admin panel", R.drawable.settings_account, true);\n                        textCell.setImageLeft(23);\n                    } else if (position == addToGroupButtonRow) {\n'
)
replace_once(
    '                    } else if (position == premiumGiftingRow) {\n                        textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == botPermissionLocation) {\n',
    '                    } else if (position == premiumGiftingRow) {\n                        textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == giftCreatorRow) {\n                        textCell.setTextAndIcon("Gift qoshish", R.drawable.menu_gift_add, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == giftStoreRow) {\n                        textCell.setTextAndIcon("Giftlar dokoni", R.drawable.menu_gift, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == botPermissionLocation) {\n'
)

# Enable custom rows.
replace_once(
    '                        position == addToGroupButtonRow || position == premiumRow || position == premiumGiftingRow ||\n                        position == businessRow || position == liteModeRow || position == birthdayRow || position == channelRow ||\n',
    '                        position == addToGroupButtonRow || position == adminPanelRow || position == premiumRow || position == premiumGiftingRow ||\n                        position == giftCreatorRow || position == giftStoreRow || position == businessRow || position == liteModeRow || position == birthdayRow || position == channelRow ||\n'
)

# View type: custom rows are ordinary text cells.
replace_once(
    '                    position == addToContactsRow || position == liteModeRow || position == premiumGiftingRow || position == businessRow ||\n',
    '                    position == addToContactsRow || position == liteModeRow || position == adminPanelRow || position == premiumGiftingRow || position == giftCreatorRow || position == giftStoreRow || position == businessRow ||\n'
)

# Search-row mapping.
replace_once(
    '            put(++pointer, devicesRow, sparseIntArray);\n            put(++pointer, devicesSectionRow, sparseIntArray);\n',
    '            put(++pointer, devicesRow, sparseIntArray);\n            put(++pointer, devicesSectionRow, sparseIntArray);\n            put(++pointer, adminPanelRow, sparseIntArray);\n            put(++pointer, giftCreatorRow, sparseIntArray);\n            put(++pointer, giftStoreRow, sparseIntArray);\n'
)

path.write_text(s, encoding='utf-8')
print('Applied SuperMe admin panel + service gift store/creator integration')
