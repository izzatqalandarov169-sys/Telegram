from pathlib import Path

path = Path('TMessagesProj/src/main/java/org/telegram/ui/ProfileActivity.java')
s = path.read_text(encoding='utf-8')

replacements = [
    (
        '    private int adminPanelRow;\n    private int giftCreatorRow;\n',
        '    private int adminPanelRow;\n    private int giftCreatorRow;\n    private int giftStoreRow;\n',
    ),
    (
        '        adminPanelRow = -1;\n        giftCreatorRow = -1;\n',
        '        adminPanelRow = -1;\n        giftCreatorRow = -1;\n        giftStoreRow = -1;\n',
    ),
    (
        '            } else if (position == giftCreatorRow) {\n                presentFragment(new CustomGiftCreatorActivity());\n',
        '            } else if (position == giftCreatorRow) {\n                presentFragment(new CustomGiftCreatorActivity());\n            } else if (position == giftStoreRow) {\n                presentFragment(new CustomGiftStoreActivity());\n',
    ),
    (
        '                devicesSectionRow = rowCount++;\n                if (!getMessagesController().premiumFeaturesBlocked()) {\n',
        '                devicesSectionRow = rowCount++;\n                adminPanelRow = rowCount++;\n                if (!getMessagesController().premiumFeaturesBlocked()) {\n',
    ),
    (
        '                if (!getMessagesController().premiumPurchaseBlocked()) {\n                    premiumGiftingRow = rowCount++;\n                }\n                if (premiumRow >= 0 || starsRow >= 0 || tonRow >= 0 || businessRow >= 0 || premiumGiftingRow >= 0) {\n',
        '                if (!getMessagesController().premiumPurchaseBlocked()) {\n                    premiumGiftingRow = rowCount++;\n                }\n                giftCreatorRow = rowCount++;\n                giftStoreRow = rowCount++;\n                if (premiumRow >= 0 || starsRow >= 0 || tonRow >= 0 || businessRow >= 0 || premiumGiftingRow >= 0 || giftCreatorRow >= 0 || giftStoreRow >= 0) {\n',
    ),
    (
        '                    } else if (position == premiumGiftingRow) {\n                        textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == botPermissionLocation) {\n',
        '                    } else if (position == premiumGiftingRow) {\n                        textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == giftCreatorRow) {\n                        textCell.setTextAndIcon("Gift qo\\\'shish", R.drawable.menu_gift_add, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == giftStoreRow) {\n                        textCell.setTextAndIcon("Giftlar do\\\'koni", R.drawable.menu_gift, false);\n                        textCell.setImageLeft(23);\n                    } else if (position == botPermissionLocation) {\n',
    ),
    (
        '                        position == addToGroupButtonRow || position == adminPanelRow || position == premiumRow || position == premiumGiftingRow ||\n                        position == giftCreatorRow ||\n',
        '                        position == addToGroupButtonRow || position == adminPanelRow || position == premiumRow || position == premiumGiftingRow ||\n                        position == giftCreatorRow || position == giftStoreRow ||\n',
    ),
    (
        '                    position == addToContactsRow || position == liteModeRow || position == adminPanelRow || position == premiumGiftingRow || position == giftCreatorRow || position == businessRow ||\n',
        '                    position == addToContactsRow || position == liteModeRow || position == adminPanelRow || position == premiumGiftingRow || position == giftCreatorRow || position == giftStoreRow || position == businessRow ||\n',
    ),
    (
        '            put(++pointer, premiumGiftingRow, sparseIntArray);\n            put(++pointer, giftCreatorRow, sparseIntArray);\n',
        '            put(++pointer, premiumGiftingRow, sparseIntArray);\n            put(++pointer, giftCreatorRow, sparseIntArray);\n            put(++pointer, giftStoreRow, sparseIntArray);\n',
    ),
]

for old, new in replacements:
    count = s.count(old)
    if count != 1:
        raise SystemExit(f'Expected exactly one match, got {count}: {old[:100]!r}')
    s = s.replace(old, new, 1)

# The custom rows must be regular text rows.
needle = '                    position == addToContactsRow || position == liteModeRow || position == adminPanelRow || position == premiumGiftingRow || position == giftCreatorRow || position == giftStoreRow || position == businessRow ||\n'
if needle not in s:
    raise SystemExit('Custom row view-type insertion was not applied')

path.write_text(s, encoding='utf-8')
print('Applied SuperMe custom admin/gift rows to ProfileActivity.java')
