
#if __has_include("RCTConvert.h")
#import "RCTConvert.h"
#else
#import <React/RCTConvert.h>
#endif

#import "RNDurationPicker.h"
#import "ActionSheetDatePicker.h"

@implementation RNDurationPicker
{
    NSHashTable *_datePickers;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(open:(NSDictionary *)args withResolver:(RCTPromiseResolveBlock)resolve andRejecter:(RCTPromiseRejectBlock)reject)
{
    UIViewController *presentingController = RCTPresentedViewController();
    
    if (presentingController == nil) {
        reject(@"E_NO_WINDOW", @"Tried to open a duration picker but there is no Window", nil);
        return;
    }
    
    NSInteger hour = [RCTConvert NSInteger:args[@"hour"]];
    NSInteger minute = [RCTConvert NSInteger:args[@"minute"]];
    NSInteger interval = [RCTConvert NSInteger:args[@"interval"]];
    NSString *title = [RCTConvert NSString:args[@"title"]];
    
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Done" style:UIBarButtonItemStyleDone target:nil action:nil];
    UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithTitle:@"Cancel" style:UIBarButtonItemStyleDone target:nil action:nil];
    
    __weak __typeof(self) weakSelf = self;
    ActionSheetDatePicker *picker = [[ActionSheetDatePicker alloc]
                                     initWithTitle:title
                                     datePickerMode:UIDatePickerModeCountDownTimer
                                     selectedDate:[NSDate new]
                                     selectedDuration:(hour * 60 * 60) + (minute * 60)
                                     doneBlock:^(ActionSheetDatePicker *picker, NSDate *selectedDate, NSTimeInterval selectedDuration, id origin) {
                                         __typeof(self) strongSelf = weakSelf;
                                         if (!strongSelf) {
                                             return;
                                         }
                                         NSInteger hour = selectedDuration / (60 * 60);
                                         NSInteger minute = (long)(selectedDuration / 60) % 60;
                                         resolve(@{@"action": @"setAction", @"hour": @(hour), @"minute": @(minute)});
                                     } cancelBlock:^(ActionSheetDatePicker *picker) {
                                         resolve(@{@"action": @"cancelAction"});
                                     } origin:presentingController.view];
    picker.minuteInterval = interval;
    picker.tapDismissAction = TapActionCancel;
    [picker setDoneButton:doneButton];
    [picker setCancelButton:cancelButton];
    [picker showActionSheetPicker];
    [[self getDatePickers] addObject:picker];
}

- (NSHashTable *) getDatePickers
{
    if (!_datePickers) {
        _datePickers = [NSHashTable weakObjectsHashTable];
    }
    return _datePickers;
}

- (void) dealloc
{
    for (ActionSheetDatePicker *picker in [self getDatePickers]) {
        if ([picker respondsToSelector:@selector(hidePickerWithCancelAction)]) {
            [picker hidePickerWithCancelAction];
        }
    }
}

@end
