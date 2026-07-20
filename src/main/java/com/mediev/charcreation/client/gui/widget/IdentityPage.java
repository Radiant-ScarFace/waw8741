package com.mediev.charcreation.client.gui.wizard;

import com.mediev.charcreation.client.gui.MedievalTheme;
import com.mediev.charcreation.client.gui.ThemedButtonWidget;
import com.mediev.charcreation.client.gui.layout.ResponsiveLayout;
import com.mediev.charcreation.data.Background;
import com.mediev.charcreation.data.Gender;
import com.mediev.charcreation.data.Nationality;
import com.mediev.charcreation.validation.CharacterValidationResult;
import com.mediev.charcreation.validation.CharacterValidator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Page 1 - Identity. Same fields, same validation rules, same behavior as
 * the original single-page screen; only the layout math changed to be
 * responsive and the widgets now live inside a wizard page instead of the
 * screen itself.
 */
public final class IdentityPage implements WizardPage {
    private final Consumer<Gender> onGenderChanged;

    private TextFieldWidget firstNameField;
    private TextFieldWidget lastNameField;
    private TextFieldWidget dayField;
    private TextFieldWidget monthField;
    private TextFieldWidget yearField;
    private ThemedButtonWidget nationalityButton;
    private ThemedButtonWidget genderButton;
    private ThemedButtonWidget backgroundButton;

    private Nationality nationality = Nationality.values()[0];
    private Gender gender = Gender.values()[0];
    private Background background = Background.values()[0];

    private String errorMessage = "";

    public IdentityPage(Consumer<Gender> onGenderChanged) {
        this.onGenderChanged = onGenderChanged;
    }

    @Override
    public String getTitle() {
        return "Identity";
    }

    @Override
    public void init(WizardHost host, ResponsiveLayout layout) {
        firstNameField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 10, 20, Text.literal("First Name"));
        firstNameField.setMaxLength(16);
        firstNameField.setPlaceholder(Text.literal("First Name"));

        lastNameField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 10, 20, Text.literal("Last Name"));
        lastNameField.setMaxLength(16);
        lastNameField.setPlaceholder(Text.literal("Last Name"));

        nationalityButton = ThemedButtonWidget.create(0, 0, 10, 20,
                Text.literal(nationality.getDisplayName()), btn -> cycleNationality());

        genderButton = ThemedButtonWidget.create(0, 0, 10, 20,
                Text.literal(gender.getDisplayName()), btn -> cycleGender());

        dayField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 10, 20, Text.literal("Day"));
        dayField.setMaxLength(2);
        dayField.setPlaceholder(Text.literal("DD"));

        monthField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 10, 20, Text.literal("Month"));
        monthField.setMaxLength(2);
        monthField.setPlaceholder(Text.literal("MM"));

        yearField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 10, 20, Text.literal("Year"));
        yearField.setMaxLength(4);
        yearField.setPlaceholder(Text.literal("YYYY"));

        backgroundButton = ThemedButtonWidget.create(0, 0, 10, 20,
                Text.literal(background.getDisplayName()), btn -> cycleBackground());

        host.addWidget(firstNameField);
        host.addWidget(lastNameField);
        host.addWidget(nationalityButton);
        host.addWidget(genderButton);
        host.addWidget(dayField);
        host.addWidget(monthField);
        host.addWidget(yearField);
        host.addWidget(backgroundButton);

        updateLayout(layout);
    }

    @Override
    public void updateLayout(ResponsiveLayout layout) {
        int left = layout.rightPanelX;
        int top = layout.rightPanelY;
        int panelWidth = layout.rightPanelWidth;
        int rowHeight = Math.max(18, layout.rightPanelHeight / 12);
        int gap = Math.max(4, rowHeight / 5);

        int y = top;
        firstNameField.setX(left);
        firstNameField.setY(y);
        firstNameField.setWidth(panelWidth);
        firstNameField.setHeight(rowHeight);
        y += rowHeight + gap;

        lastNameField.setX(left);
        lastNameField.setY(y);
        lastNameField.setWidth(panelWidth);
        lastNameField.setHeight(rowHeight);
        y += rowHeight + gap;

        nationalityButton.setX(left);
        nationalityButton.setY(y);
        nationalityButton.setWidth(panelWidth);
        nationalityButton.setHeight(rowHeight);
        y += rowHeight + gap;

        genderButton.setX(left);
        genderButton.setY(y);
        genderButton.setWidth(panelWidth);
        genderButton.setHeight(rowHeight);
        y += rowHeight + gap;

        int dobGap = Math.max(4, panelWidth / 30);
        int dobWidth = (panelWidth - dobGap * 2) / 3;
        dayField.setX(left);
        dayField.setY(y);
        dayField.setWidth(dobWidth);
        dayField.setHeight(rowHeight);

        monthField.setX(left + dobWidth + dobGap);
        monthField.setY(y);
        monthField.setWidth(dobWidth);
        monthField.setHeight(rowHeight);

        yearField.setX(left + (dobWidth + dobGap) * 2);
        yearField.setY(y);
        yearField.setWidth(panelWidth - (dobWidth + dobGap) * 2);
        yearField.setHeight(rowHeight);
        y += rowHeight + gap;

        backgroundButton.setX(left);
        backgroundButton.setY(y);
        backgroundButton.setWidth(panelWidth);
        backgroundButton.setHeight(rowHeight);
    }

    @Override
    public void tick() {
        CharacterValidationResult result = validate();
        errorMessage = result.isValid() ? "" : result.getErrorMessage();
    }

    @Override
    public void renderContent(DrawContext context, int mouseX, int mouseY, float delta, ResponsiveLayout layout) {
        if (!errorMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(errorMessage),
                    layout.rightPanelX + layout.rightPanelWidth / 2, layout.rightPanelY + layout.rightPanelHeight + 6,
                    MedievalTheme.ERROR_COLOR);
        }
    }

    @Override
    public boolean isValid() {
        return validate().isValid();
    }

    private CharacterValidationResult validate() {
        return CharacterValidator.validate(
                firstNameField.getText(),
                lastNameField.getText(),
                parseIntSafe(dayField.getText()),
                parseIntSafe(monthField.getText()),
                parseIntSafe(yearField.getText())
        );
    }

    private void cycleNationality() {
        Nationality[] values = Nationality.values();
        nationality = values[(nationality.ordinal() + 1) % values.length];
        nationalityButton.setMessage(Text.literal(nationality.getDisplayName()));
    }

    private void cycleGender() {
        Gender[] values = Gender.values();
        gender = values[(gender.ordinal() + 1) % values.length];
        genderButton.setMessage(Text.literal(gender.getDisplayName()));
        onGenderChanged.accept(gender);
    }

    private void cycleBackground() {
        Background[] values = Background.values();
        background = values[(background.ordinal() + 1) % values.length];
        backgroundButton.setMessage(Text.literal(background.getDisplayName()));
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getFirstName() {
        return firstNameField.getText().trim();
    }

    public String getLastName() {
        return lastNameField.getText().trim();
    }

    public Nationality getNationality() {
        return nationality;
    }

    public Gender getGender() {
        return gender;
    }

    public Background getBackground() {
        return background;
    }

    public int getBirthDay() {
        return parseIntSafe(dayField.getText());
    }

    public int getBirthMonth() {
        return parseIntSafe(monthField.getText());
    }

    public int getBirthYear() {
        return parseIntSafe(yearField.getText());
    }
}